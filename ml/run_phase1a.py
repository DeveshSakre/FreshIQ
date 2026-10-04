import time
import json
from pathlib import Path
import pandas as pd
import torch
import torch.nn as nn
from torch.utils.data import DataLoader

from ml.config import TrainingConfig
from ml.dataset import (
    AvocadoDataset,
    verify_and_load_splits,
    get_train_transforms,
    get_eval_transforms,
    compute_class_weights,
)
from ml.model import build_mobilenet_v3_small
from ml.train import run_epoch
from ml.evaluate import evaluate_test_set


def main():
    config = TrainingConfig()
    config.set_seed()
    device = config.device
    start_time = time.time()

    print("=" * 60, flush=True)
    print("FRESHIQ PHASE 1A: MOBILENETV3-SMALL FULL TRAINING RUN", flush=True)
    print(f"Device: {device}", flush=True)
    print("=" * 60, flush=True)

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
    best_epoch = -1
    history = []
    best_model_path = config.get_best_model_path()

    print(f"\nStarting 12-Epoch Training...\n", flush=True)

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
            best_epoch = epoch
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

        star = " [BEST]" if is_best else ""
        print(f"Epoch {epoch}/{config.num_epochs}{star}", flush=True)
        print(f"Train Loss:     {train_loss:.4f} (Acc: {train_acc:.4f}, F1: {train_f1:.4f})", flush=True)
        print(f"Val Loss:       {val_loss:.4f}", flush=True)
        print(f"Val Accuracy:   {val_acc:.4f}", flush=True)
        print(f"Val Macro-F1:   {val_f1:.4f} (Prec: {val_prec:.4f}, Rec: {val_rec:.4f})", flush=True)
        print(f"Time:           {elapsed:.1f}s\n", flush=True)

    total_training_sec = time.time() - start_time

    history_path = config.model_dir / "training_history.json"
    with open(history_path, "w") as f:
        json.dump(history, f, indent=2)

    print("=" * 60, flush=True)
    print(f"Best Epoch:        {best_epoch}", flush=True)
    print(f"Best Val Macro-F1: {best_val_f1:.4f}", flush=True)
    print("=" * 60, flush=True)

    # 4. Evaluate untouched test set on best checkpoint
    print("\nRunning Evaluation on Untouched Test Set...", flush=True)
    test_metrics = evaluate_test_set(config, best_model_path)

    print("\n" + "=" * 60, flush=True)
    print("TEST RESULTS", flush=True)
    print("=" * 60, flush=True)
    print(f"Accuracy:        {test_metrics['test_accuracy']:.4f}", flush=True)
    print(f"Macro Precision: {test_metrics['test_macro_precision']:.4f}", flush=True)
    print(f"Macro Recall:    {test_metrics['test_macro_recall']:.4f}", flush=True)
    print(f"Macro F1:        {test_metrics['test_macro_f1']:.4f}", flush=True)

    print("\nClassification Report:", flush=True)
    target_names = [config.stage_labels[i] for i in range(1, config.num_classes + 1)]
    report_dict = test_metrics["classification_report"]

    per_class_df = pd.DataFrame([
        {
            "Stage": f"Stage {i}",
            "Class Label": target_names[i-1],
            "Precision": f"{report_dict[target_names[i-1]]['precision']:.4f}",
            "Recall": f"{report_dict[target_names[i-1]]['recall']:.4f}",
            "F1-Score": f"{report_dict[target_names[i-1]]['f1-score']:.4f}",
            "Support": int(report_dict[target_names[i-1]]["support"])
        }
        for i in range(1, config.num_classes + 1)
    ])
    print(per_class_df.to_string(index=False), flush=True)

    print("\nConfusion Matrix (Actual \\ Predicted):", flush=True)
    cm_df = pd.DataFrame(
        test_metrics["confusion_matrix"],
        index=[f"Actual Stage {i}" for i in range(1, 6)],
        columns=[f"Pred Stage {i}" for i in range(1, 6)]
    )
    print(cm_df.to_string(), flush=True)

    print(f"\nFinal Checkpoint Path: {best_model_path.resolve()}", flush=True)
    print(f"Total Time:            {total_training_sec / 60.0:.2f} minutes ({total_training_sec:.1f} seconds)", flush=True)
    print("=" * 60, flush=True)


if __name__ == "__main__":
    main()
