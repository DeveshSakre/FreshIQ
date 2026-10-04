import json
import time
from pathlib import Path
from typing import Dict, Tuple

import numpy as np
import pandas as pd
import torch
import torch.nn as nn
from torch.utils.data import DataLoader

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
)

from ml.config import TrainingConfig
from ml.dataset import (
    AvocadoDataset,
    verify_and_load_splits,
    get_train_transforms,
    get_eval_transforms,
    compute_class_weights,
)
from ml.model import build_mobilenet_v3_small


def run_epoch(
    model: nn.Module,
    loader: DataLoader,
    criterion: nn.Module,
    device: str,
    optimizer: torch.optim.Optimizer = None,
) -> Tuple[float, float, float, float, float]:
    """
    Runs one epoch over the dataset loader.
    If optimizer is provided, runs training step (model.train()), otherwise eval step (model.eval()).
    Returns: (loss, accuracy, macro_precision, macro_recall, macro_f1)
    """
    is_train = optimizer is not None
    model.train(is_train)

    total_loss = 0.0
    y_true = []
    y_pred = []

    total_batches = len(loader)
    for batch_idx, (images, labels, _) in enumerate(loader, 1):
        images = images.to(device, non_blocking=True)
        labels = labels.to(device, non_blocking=True)

        if is_train:
            optimizer.zero_grad(set_to_none=True)

        logits = model(images)
        loss = criterion(logits, labels)

        if is_train:
            loss.backward()
            optimizer.step()

        total_loss += loss.item() * images.size(0)
        preds = logits.argmax(dim=1)

        y_true.extend(labels.detach().cpu().numpy())
        y_pred.extend(preds.detach().cpu().numpy())

        if is_train and (batch_idx % 20 == 0 or batch_idx == total_batches):
            print(f"   Batch [{batch_idx:03d}/{total_batches:03d}] - Loss: {loss.item():.4f}", flush=True)

    avg_loss = total_loss / len(loader.dataset)
    accuracy = accuracy_score(y_true, y_pred)
    precision = precision_score(y_true, y_pred, average="macro", zero_division=0)
    recall = recall_score(y_true, y_pred, average="macro", zero_division=0)
    f1 = f1_score(y_true, y_pred, average="macro", zero_division=0)

    return avg_loss, accuracy, precision, recall, f1


def train(config: TrainingConfig):
    """
    Main training function for FreshIQ avocado ripeness classifier.
    Saves best model based on validation macro-F1.
    """
    config.set_seed()
    device = config.device
    print(f"Starting FreshIQ Training on Device: {device}")

    # 1. Load & verify splits
    train_df, val_df, test_df = verify_and_load_splits(config)

    # 2. Datasets & Loaders
    train_transform = get_train_transforms(config.image_size, config.imagenet_mean, config.imagenet_std)
    eval_transform = get_eval_transforms(config.image_size, config.imagenet_mean, config.imagenet_std)

    train_ds = AvocadoDataset(train_df, config.data_root, train_transform)
    val_ds = AvocadoDataset(val_df, config.data_root, eval_transform)

    train_loader = DataLoader(
        train_ds,
        batch_size=config.batch_size,
        shuffle=True,
        num_workers=config.num_workers,
        pin_memory=(device == "cuda")
    )
    val_loader = DataLoader(
        val_ds,
        batch_size=config.batch_size,
        shuffle=False,
        num_workers=config.num_workers,
        pin_memory=(device == "cuda")
    )

    # 3. Model & Loss & Optimizer
    model = build_mobilenet_v3_small(
        num_classes=config.num_classes,
        pretrained=True,
        freeze_backbone=True
    ).to(device)

    class_weights = compute_class_weights(train_df, config.num_classes, device=device)
    criterion = nn.CrossEntropyLoss(weight=class_weights)

    optimizer = torch.optim.AdamW(
        filter(lambda p: p.requires_grad, model.parameters()),
        lr=config.learning_rate,
        weight_decay=config.weight_decay
    )

    best_val_f1 = -1.0
    history = []
    best_model_path = config.get_best_model_path()

    print(f"\nTraining for {config.num_epochs} epochs...")

    for epoch in range(1, config.num_epochs + 1):
        t0 = time.time()
        train_loss, train_acc, train_prec, train_rec, train_f1 = run_epoch(
            model, train_loader, criterion, device, optimizer
        )

        with torch.no_grad():
            val_loss, val_acc, val_prec, val_rec, val_f1 = run_epoch(
                model, val_loader, criterion, device
            )

        elapsed = time.time() - t0

        epoch_stats = {
            "epoch": epoch,
            "train_loss": train_loss,
            "train_accuracy": train_acc,
            "train_macro_f1": train_f1,
            "val_loss": val_loss,
            "val_accuracy": val_acc,
            "val_macro_precision": val_prec,
            "val_macro_recall": val_rec,
            "val_macro_f1": val_f1,
            "time_sec": elapsed
        }
        history.append(epoch_stats)

        is_best = val_f1 > best_val_f1
        if is_best:
            best_val_f1 = val_f1
            checkpoint = {
                "model_state_dict": model.state_dict(),
                "model_name": "mobilenet_v3_small",
                "class_to_idx": config.class_to_idx,
                "stage_labels": config.stage_labels,
                "preprocessing_config": {
                    "image_size": config.image_size,
                    "mean": config.imagenet_mean,
                    "std": config.imagenet_std
                },
                "training_config": {
                    "learning_rate": config.learning_rate,
                    "batch_size": config.batch_size,
                    "epochs": config.num_epochs,
                    "seed": config.seed
                },
                "best_epoch": epoch,
                "best_val_macro_f1": float(best_val_f1),
                "best_val_accuracy": float(val_acc)
            }
            torch.save(checkpoint, best_model_path)

        star = " * BEST" if is_best else ""
        print(
            f"Epoch {epoch:02d}/{config.num_epochs:02d} [{elapsed:.1f}s] | "
            f"Train Loss={train_loss:.4f} Acc={train_acc:.4f} F1={train_f1:.4f} | "
            f"Val Loss={val_loss:.4f} Acc={val_acc:.4f} Prec={val_prec:.4f} Rec={val_rec:.4f} F1={val_f1:.4f}{star}",
            flush=True
        )

    print(f"\nTraining Complete! Best Validation Macro-F1: {best_val_f1:.4f}")
    print(f"Saved Checkpoint: {best_model_path.resolve()}")

    history_path = config.model_dir / "training_history.json"
    with open(history_path, "w") as f:
        json.dump(history, f, indent=2)

    return model, history
