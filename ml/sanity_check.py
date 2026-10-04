import sys
import traceback
from pathlib import Path
import torch
import torch.nn as nn
from torch.utils.data import DataLoader

from ml.config import TrainingConfig
from ml.dataset import (
    AvocadoDataset,
    verify_and_load_splits,
    get_train_transforms,
    compute_class_weights,
)
from ml.model import build_mobilenet_v3_small


def run_sanity_check():
    """
    Runs a small, end-to-end pipeline verification check using 2 batches of batch_size=4.
    Tests:
    CSV manifest -> image loading -> preprocessing -> DataLoader -> MobileNetV3-Small ->
    forward pass -> loss calculation -> backward pass -> optimizer step.
    """
    print("=" * 60)
    print("FRESHIQ PIPELINE SANITY CHECK (PHASE 1A)")
    print("=" * 60)

    try:
        config = TrainingConfig()
        config.set_seed()
        device = config.device

        print(f"Device: {device}")

        # 1. Verification & splits load
        train_df, val_df, test_df = verify_and_load_splits(config)

        # 2. Datasets & Loaders (small batch size for sanity check)
        train_transform = get_train_transforms(config.image_size, config.imagenet_mean, config.imagenet_std)
        train_ds = AvocadoDataset(train_df, config.data_root, train_transform)

        sanity_batch_size = 4
        sanity_loader = DataLoader(
            train_ds,
            batch_size=sanity_batch_size,
            shuffle=True,
            num_workers=0
        )

        # 3. Model construction
        print("\nLoading MobileNetV3-Small model...")
        model = build_mobilenet_v3_small(
            num_classes=config.num_classes,
            pretrained=True,
            freeze_backbone=True
        ).to(device)
        model.train()

        # 4. Class weights & loss & optimizer
        class_weights = compute_class_weights(train_df, config.num_classes, device=device)
        criterion = nn.CrossEntropyLoss(weight=class_weights)
        optimizer = torch.optim.AdamW(
            filter(lambda p: p.requires_grad, model.parameters()),
            lr=config.learning_rate
        )

        # 5. Pipeline execution check across 2 batches
        print("\nRunning forward/backward pipeline test on 2 mini-batches...")

        batch_shapes = []
        output_shapes = []
        losses = []

        for i, (images, labels, spec_ids) in enumerate(sanity_loader):
            if i >= 2:
                break

            images = images.to(device)
            labels = labels.to(device)

            batch_shapes.append(list(images.shape))

            # Forward pass
            optimizer.zero_grad()
            logits = model(images)
            output_shapes.append(list(logits.shape))

            # Loss calculation
            loss = criterion(logits, labels)
            loss_val = float(loss.item())
            losses.append(loss_val)

            # Backward pass
            loss.backward()

            # Verify gradients exist before step
            trainable_params = [p for p in model.parameters() if p.requires_grad]
            grad_norms = [p.grad.norm().item() for p in trainable_params if p.grad is not None]

            if len(grad_norms) == 0:
                raise RuntimeError("No gradients were calculated during loss.backward()!")

            # Optimizer step
            optimizer.step()

        print("\n" + "=" * 60)
        print("FRESHIQ SANITY CHECK RESULTS")
        print("=" * 60)
        print("Status:                 PASSED")
        print("Errors:                 None")
        print(f"Input Image Tensor Shape: {batch_shapes[0]}")
        print(f"Model Output Logits Shape: {output_shapes[0]}")
        print(f"Loss Value (Batch 1):    {losses[0]:.4f}")
        print(f"Loss Value (Batch 2):    {losses[1]:.4f}")
        print("Backpropagation Step:   SUCCESSFUL (Gradients computed)")
        print("Optimizer Step:         SUCCESSFUL (Weights updated)")
        print("=" * 60)
        return True

    except Exception as e:
        print("\n" + "=" * 60)
        print("FRESHIQ SANITY CHECK RESULTS")
        print("=" * 60)
        print("Status:                 FAILED")
        print(f"Error Type:             {type(e).__name__}")
        print(f"Error Message:          {str(e)}")
        print("\nTraceback:")
        traceback.print_exc()
        print("=" * 60)
        return False


if __name__ == "__main__":
    success = run_sanity_check()
    sys.exit(0 if success else 1)
