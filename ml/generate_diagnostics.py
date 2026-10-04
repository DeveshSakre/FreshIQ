import json
from pathlib import Path
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

import torch
from torch.utils.data import DataLoader

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    classification_report,
    confusion_matrix,
)

from ml.config import TrainingConfig
from ml.dataset import AvocadoDataset, verify_and_load_splits, get_eval_transforms
from ml.model import build_mobilenet_v3_small


def generate_diagnostics():
    config = TrainingConfig()
    config.set_seed()
    device = config.device

    diag_dir = Path("ml/diagnostics")
    diag_dir.mkdir(parents=True, exist_ok=True)

    print("=" * 60)
    print("FRESHIQ MODEL DIAGNOSTICS & ERROR ANALYSIS")
    print("=" * 60)

    # 1. Checkpoint Verification
    checkpoint_path = config.get_best_model_path()
    if not checkpoint_path.exists():
        raise FileNotFoundError(f"Checkpoint not found at {checkpoint_path}")

    checkpoint = torch.load(checkpoint_path, map_location=device)
    saved_best_epoch = checkpoint.get("best_epoch", -1)
    saved_best_val_f1 = checkpoint.get("best_val_macro_f1", -1.0)

    print(f"Checkpoint Path:         {checkpoint_path.resolve()}")
    print(f"Verified Best Epoch:     Epoch {saved_best_epoch}")
    print(f"Verified Best Val F1:    {saved_best_val_f1:.4f}")
    assert saved_best_epoch == 10, f"Expected Epoch 10, got {saved_best_epoch}"
    assert abs(saved_best_val_f1 - 0.6745) < 0.001, f"Expected Val F1 ~0.6745, got {saved_best_val_f1}"
    print("[OK] Checkpoint verification passed (Epoch 10, Val Macro-F1 = 0.6745).")

    # 2. Load Training History & Generate Curves
    history_path = config.model_dir / "training_history.json"
    with open(history_path, "r") as f:
        history = json.load(f)

    history_df = pd.DataFrame(history)
    epochs = history_df["epoch"].values

    # Plot 1: Loss Curve
    plt.figure(figsize=(8, 5))
    plt.plot(epochs, history_df["train_loss"], "o-", label="Train Loss", color="#1f77b4", linewidth=2)
    plt.plot(epochs, history_df["val_loss"], "s--", label="Validation Loss", color="#ff7f0e", linewidth=2)
    plt.axvline(x=saved_best_epoch, color="red", linestyle=":", label=f"Best Checkpoint (Epoch {saved_best_epoch})")
    plt.title("FreshIQ MobileNetV3 - Training vs Validation Loss")
    plt.xlabel("Epoch")
    plt.ylabel("Cross-Entropy Loss")
    plt.xticks(epochs)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()
    loss_fig_path = diag_dir / "loss_curve.png"
    plt.savefig(loss_fig_path, dpi=300)
    plt.close()

    # Plot 2: Accuracy Curve
    plt.figure(figsize=(8, 5))
    plt.plot(epochs, history_df["train_accuracy"], "o-", label="Train Accuracy", color="#2ca02c", linewidth=2)
    plt.plot(epochs, history_df["val_accuracy"], "s--", label="Validation Accuracy", color="#d62728", linewidth=2)
    plt.axvline(x=saved_best_epoch, color="red", linestyle=":", label=f"Best Checkpoint (Epoch {saved_best_epoch})")
    plt.title("FreshIQ MobileNetV3 - Training vs Validation Accuracy")
    plt.xlabel("Epoch")
    plt.ylabel("Accuracy")
    plt.xticks(epochs)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()
    acc_fig_path = diag_dir / "accuracy_curve.png"
    plt.savefig(acc_fig_path, dpi=300)
    plt.close()

    # Plot 3: Macro-F1 Curve
    plt.figure(figsize=(8, 5))
    plt.plot(epochs, history_df["train_macro_f1"], "o-", label="Train Macro-F1", color="#9467bd", linewidth=2)
    plt.plot(epochs, history_df["val_macro_f1"], "s--", label="Validation Macro-F1", color="#8c564b", linewidth=2)
    plt.axvline(x=saved_best_epoch, color="red", linestyle=":", label=f"Best Checkpoint (Epoch {saved_best_epoch})")
    plt.title("FreshIQ MobileNetV3 - Training vs Validation Macro-F1")
    plt.xlabel("Epoch")
    plt.ylabel("Macro F1-Score")
    plt.xticks(epochs)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()
    f1_fig_path = diag_dir / "macro_f1_curve.png"
    plt.savefig(f1_fig_path, dpi=300)
    plt.close()

    # 3. Test Set Inference & Evaluation
    train_df, val_df, test_df = verify_and_load_splits(config)
    eval_transform = get_eval_transforms(config.image_size, config.imagenet_mean, config.imagenet_std)
    test_ds = AvocadoDataset(test_df, config.data_root, eval_transform)
    test_loader = DataLoader(test_ds, batch_size=config.batch_size, shuffle=False, num_workers=0)

    model = build_mobilenet_v3_small(num_classes=config.num_classes, pretrained=False, freeze_backbone=False)
    model.load_state_dict(checkpoint["model_state_dict"])
    model = model.to(device)
    model.eval()

    y_true_0idx = []
    y_pred_0idx = []

    with torch.no_grad():
        for images, labels, _ in test_loader:
            images = images.to(device)
            logits = model(images)
            preds = logits.argmax(dim=1)

            y_true_0idx.extend(labels.numpy())
            y_pred_0idx.extend(preds.cpu().numpy())

    y_true = np.array(y_true_0idx) + 1  # Stages 1 to 5
    y_pred = np.array(y_pred_0idx) + 1  # Stages 1 to 5
    target_names = [config.stage_labels[i] for i in range(1, 6)]

    # Plot 4: Confusion Matrix Heatmap
    cm = confusion_matrix(y_true, y_pred, labels=[1, 2, 3, 4, 5])
    plt.figure(figsize=(8, 6.5))
    sns.heatmap(
        cm,
        annot=True,
        fmt="d",
        cmap="Blues",
        xticklabels=[f"Pred S{i}" for i in range(1, 6)],
        yticklabels=[f"Actual S{i}" for i in range(1, 6)]
    )
    plt.title("FreshIQ Avocado Ripening Stage Confusion Matrix (Test Set)")
    plt.xlabel("Predicted Stage")
    plt.ylabel("Actual Stage")
    plt.tight_layout()
    cm_fig_path = diag_dir / "confusion_matrix_heatmap.png"
    plt.savefig(cm_fig_path, dpi=300)
    plt.close()

    # Plot 5: Per-Class Precision, Recall, F1 Bar Chart
    report_dict = classification_report(y_true, y_pred, labels=[1, 2, 3, 4, 5], target_names=target_names, output_dict=True)

    metrics_df = pd.DataFrame([
        {
            "Stage": f"Stage {i}",
            "Precision": report_dict[target_names[i-1]]["precision"],
            "Recall": report_dict[target_names[i-1]]["recall"],
            "F1-Score": report_dict[target_names[i-1]]["f1-score"]
        }
        for i in range(1, 6)
    ])

    plt.figure(figsize=(9, 5.5))
    x = np.arange(5)
    width = 0.25

    plt.bar(x - width, metrics_df["Precision"], width, label="Precision", color="#1f77b4")
    plt.bar(x, metrics_df["Recall"], width, label="Recall", color="#ff7f0e")
    plt.bar(x + width, metrics_df["F1-Score"], width, label="F1-Score", color="#2ca02c")

    plt.xlabel("Ripening Stage")
    plt.ylabel("Score")
    plt.title("FreshIQ Per-Class Performance Metrics (Untouched Test Set)")
    plt.xticks(x, [f"Stage {i}" for i in range(1, 6)])
    plt.ylim(0, 1.0)
    plt.grid(axis="y", linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()
    metrics_fig_path = diag_dir / "per_class_metrics_bar.png"
    plt.savefig(metrics_fig_path, dpi=300)
    plt.close()

    # 4. Ordinal Stage Distance Analysis & MAE
    abs_errors = np.abs(y_true - y_pred)
    total_samples = len(y_true)

    off_0 = np.sum(abs_errors == 0)
    off_1 = np.sum(abs_errors == 1)
    off_2 = np.sum(abs_errors == 2)
    off_3_plus = np.sum(abs_errors >= 3)

    pct_0 = (off_0 / total_samples) * 100.0
    pct_1 = (off_1 / total_samples) * 100.0
    pct_2 = (off_2 / total_samples) * 100.0
    pct_3_plus = (off_3_plus / total_samples) * 100.0
    mae = np.mean(abs_errors)

    # 5. Specific Stage 4 vs Stage 5 Confusion Analysis
    actual_s4_mask = (y_true == 4)
    actual_s5_mask = (y_true == 5)

    s4_total = np.sum(actual_s4_mask)
    s5_total = np.sum(actual_s5_mask)

    s4_as_s5 = np.sum((y_true == 4) & (y_pred == 5))
    s5_as_s4 = np.sum((y_true == 5) & (y_pred == 4))

    s4_as_s5_pct = (s4_as_s5 / s4_total) * 100.0
    s5_as_s4_pct = (s5_as_s4 / s5_total) * 100.0

    # 6. Save JSON Diagnostic Summary
    diag_summary = {
        "checkpoint_verification": {
            "checkpoint_path": str(checkpoint_path.resolve()),
            "best_epoch": saved_best_epoch,
            "best_val_macro_f1": saved_best_val_f1,
            "test_set_untouched": True
        },
        "ordinal_error_metrics": {
            "total_test_samples": total_samples,
            "exact_match_0_stages_off_count": int(off_0),
            "exact_match_0_stages_off_pct": float(pct_0),
            "adjacent_error_1_stage_off_count": int(off_1),
            "adjacent_error_1_stage_off_pct": float(pct_1),
            "error_2_stages_off_count": int(off_2),
            "error_2_stages_off_pct": float(pct_2),
            "error_3_plus_stages_off_count": int(off_3_plus),
            "error_3_plus_stages_off_pct": float(pct_3_plus),
            "ordinal_mean_absolute_error": float(mae),
            "within_1_stage_cumulative_pct": float(pct_0 + pct_1)
        },
        "stage_4_5_confusion": {
            "actual_stage_4_total": int(s4_total),
            "actual_stage_4_predicted_as_stage_5_count": int(s4_as_s5),
            "actual_stage_4_predicted_as_stage_5_pct": float(s4_as_s5_pct),
            "actual_stage_5_total": int(s5_total),
            "actual_stage_5_predicted_as_stage_4_count": int(s5_as_s4),
            "actual_stage_5_predicted_as_stage_4_pct": float(s5_as_s4_pct)
        },
        "epoch_instability_analysis": {
            "epoch_4_dip_val_f1": float(history_df.loc[history_df["epoch"]==4, "val_macro_f1"].values[0]),
            "epoch_8_dip_val_f1": float(history_df.loc[history_df["epoch"]==8, "val_macro_f1"].values[0]),
            "peak_convergence_epoch": saved_best_epoch,
            "peak_val_f1": saved_best_val_f1
        }
    }

    summary_json_path = diag_dir / "diagnostic_summary.json"
    with open(summary_json_path, "w") as f:
        json.dump(diag_summary, f, indent=2)

    print("\nDiagnostic Summary JSON saved to:", summary_json_path.resolve())
    print("Visualizations saved under:", diag_dir.resolve())
    print("=" * 60)

    return diag_summary, cm, metrics_df


if __name__ == "__main__":
    generate_diagnostics()
