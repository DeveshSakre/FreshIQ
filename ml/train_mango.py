import os
import sys
import time
import json
import hashlib
from pathlib import Path
from typing import Dict, Tuple, List

import numpy as np
import pandas as pd
from PIL import Image
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns

import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
import torchvision.transforms as T
import torchvision.models as models

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    cohen_kappa_score,
    balanced_accuracy_score,
    confusion_matrix,
    classification_report
)

# ---------------------------------------------------------
# CONSTANTS & MAPPINGS
# ---------------------------------------------------------
CLASS_NAMES = ["Unripe", "Semiripe", "Fully Ripe", "Overripe", "Perished"]
CLASS_TO_IDX = {name: idx for idx, name in enumerate(CLASS_NAMES)}
IDX_TO_CLASS = {idx: name for idx, name in enumerate(CLASS_NAMES)}

DATASET_ROOT = Path(r"D:\FreshIQ\ml\datasets\multi_produce\Mango_preprocessed_256")
RAW_DATASET_ROOT = Path(r"D:\FreshIQ\ml\datasets\multi_produce\Mango Shelf-Life Dataset")
SPLIT_CSV_PATH = Path(r"D:\FreshIQ\ml\datasets\multi_produce\Mango\manifests\mango_train_val_test_split.csv")
SAVED_MODELS_DIR = Path(r"D:\FreshIQ\ml\saved_models")

BEST_CHECKPOINT_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_best.pth"
CONFIG_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_config.json"
TEST_EVAL_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_test_evaluation.json"
HISTORY_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_training_history.json"
CONFUSION_MATRIX_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_confusion_matrix.png"

IMAGENET_MEAN = [0.485, 0.456, 0.406]
IMAGENET_STD = [0.229, 0.224, 0.225]


# ---------------------------------------------------------
# DATASET CLASS
# ---------------------------------------------------------
class MangoDataset(Dataset):
    def __init__(self, df: pd.DataFrame, root_dir: Path, transform=None):
        self.df = df.reset_index(drop=True)
        self.root_dir = root_dir
        self.transform = transform

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        row = self.df.iloc[idx]
        cls_name = row["class"]
        filename = row["filename"]
        img_path = self.root_dir / cls_name / filename
        
        # Fallback to raw path if not in preprocessed directory
        if not img_path.exists():
            img_path = RAW_DATASET_ROOT / cls_name / filename

        with Image.open(img_path) as im:
            image = im.convert("RGB")

        if self.transform:
            image = self.transform(image)

        label = CLASS_TO_IDX[cls_name]
        return image, label, filename


# ---------------------------------------------------------
# TRANSFORMS
# ---------------------------------------------------------
def get_train_transforms():
    return T.Compose([
        T.RandomResizedCrop(224, scale=(0.85, 1.0)),
        T.RandomHorizontalFlip(p=0.5),
        T.RandomRotation(degrees=15),
        T.ColorJitter(brightness=0.1, contrast=0.1, saturation=0.1, hue=0.02),
        T.ToTensor(),
        T.Normalize(mean=IMAGENET_MEAN, std=IMAGENET_STD)
    ])


def get_eval_transforms():
    return T.Compose([
        T.Resize((224, 224)),
        T.ToTensor(),
        T.Normalize(mean=IMAGENET_MEAN, std=IMAGENET_STD)
    ])


# ---------------------------------------------------------
# MODEL BUILDER
# ---------------------------------------------------------
def build_mobilenet_v3_large(num_classes: int = 5, pretrained: bool = True) -> nn.Module:
    weights = models.MobileNet_V3_Large_Weights.DEFAULT if pretrained else None
    model = models.mobilenet_v3_large(weights=weights)
    
    # Freeze all backbone features initially
    for param in model.features.parameters():
        param.requires_grad = False

    in_features = model.classifier[3].in_features
    model.classifier[3] = nn.Linear(in_features, num_classes)
    return model


# ---------------------------------------------------------
# TRAINING & EVALUATION FUNCTIONS
# ---------------------------------------------------------
def run_epoch(
    model: nn.Module,
    loader: DataLoader,
    criterion: nn.Module,
    device: torch.device,
    optimizer: torch.optim.Optimizer = None
) -> Tuple[float, float, float, float, float]:
    is_train = optimizer is not None
    model.train(is_train)

    total_loss = 0.0
    y_true = []
    y_pred = []

    for images, labels, _ in loader:
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

    avg_loss = total_loss / len(loader.dataset)
    acc = accuracy_score(y_true, y_pred)
    macro_p = precision_score(y_true, y_pred, average="macro", zero_division=0)
    macro_r = recall_score(y_true, y_pred, average="macro", zero_division=0)
    macro_f1 = f1_score(y_true, y_pred, average="macro", zero_division=0)

    return avg_loss, acc, macro_p, macro_r, macro_f1


def evaluate_test_set(
    model: nn.Module,
    loader: DataLoader,
    device: torch.device
) -> Dict:
    model.eval()
    y_true = []
    y_pred = []
    all_probs = []

    with torch.no_grad():
        for images, labels, _ in loader:
            images = images.to(device, non_blocking=True)
            logits = model(images)
            probs = torch.softmax(logits, dim=1)
            preds = logits.argmax(dim=1)

            y_true.extend(labels.cpu().numpy())
            y_pred.extend(preds.cpu().numpy())
            all_probs.extend(probs.cpu().numpy().tolist())

    y_true = np.array(y_true)
    y_pred = np.array(y_pred)

    # Standard metrics
    acc = accuracy_score(y_true, y_pred)
    macro_p = precision_score(y_true, y_pred, average="macro", zero_division=0)
    macro_r = recall_score(y_true, y_pred, average="macro", zero_division=0)
    macro_f1 = f1_score(y_true, y_pred, average="macro", zero_division=0)

    weighted_p = precision_score(y_true, y_pred, average="weighted", zero_division=0)
    weighted_r = recall_score(y_true, y_pred, average="weighted", zero_division=0)
    weighted_f1 = f1_score(y_true, y_pred, average="weighted", zero_division=0)

    kappa = cohen_kappa_score(y_true, y_pred)
    balanced_acc = balanced_accuracy_score(y_true, y_pred)

    # Per-class metrics
    per_class_p = precision_score(y_true, y_pred, average=None, zero_division=0)
    per_class_r = recall_score(y_true, y_pred, average=None, zero_division=0)
    per_class_f1 = f1_score(y_true, y_pred, average=None, zero_division=0)
    cm = confusion_matrix(y_true, y_pred, labels=list(range(len(CLASS_NAMES))))

    # Ordinal analysis
    abs_diff = np.abs(y_true - y_pred)
    within_1_stage = float(np.mean(abs_diff <= 1))
    ordinal_mae = float(np.mean(abs_diff))

    per_class_metrics = {}
    for idx, c_name in enumerate(CLASS_NAMES):
        support = int(np.sum(y_true == idx))
        per_class_metrics[c_name] = {
            "precision": float(per_class_p[idx]),
            "recall": float(per_class_r[idx]),
            "f1_score": float(per_class_f1[idx]),
            "support": support
        }

    return {
        "accuracy": float(acc),
        "macro_precision": float(macro_p),
        "macro_recall": float(macro_r),
        "macro_f1": float(macro_f1),
        "weighted_precision": float(weighted_p),
        "weighted_recall": float(weighted_r),
        "weighted_f1": float(weighted_f1),
        "cohens_kappa": float(kappa),
        "balanced_accuracy": float(balanced_acc),
        "within_plus_minus_1_stage_accuracy": within_1_stage,
        "ordinal_stage_mae": ordinal_mae,
        "per_class_metrics": per_class_metrics,
        "confusion_matrix": cm.tolist(),
        "total_test_samples": len(y_true)
    }


# ---------------------------------------------------------
# MAIN TRAINING PIPELINE
# ---------------------------------------------------------
def train_mango():
    start_time = time.time()
    torch.manual_seed(42)
    np.random.seed(42)
    torch.set_num_threads(10)
    device = torch.device("cpu")

    print(f"=== PHASE 2J.1: MANGO 5-CLASS MODEL TRAINING ===")
    print(f"Device: {device} (Threads: {torch.get_num_threads()})")
    
    # 1. Load Split Manifest
    print(f"Loading split manifest: {SPLIT_CSV_PATH}")
    split_df = pd.read_csv(SPLIT_CSV_PATH)
    train_df = split_df[split_df["split"] == "train"].copy()
    val_df = split_df[split_df["split"] == "val"].copy()
    test_df = split_df[split_df["split"] == "test"].copy()

    print(f"Train samples: {len(train_df)}")
    print(f"Val samples:   {len(val_df)}")
    print(f"Test samples:  {len(test_df)}")

    # 2. Compute Class Weights from Train Set Only
    train_counts = train_df["class"].value_counts()
    n_train = len(train_df)
    n_classes = len(CLASS_NAMES)
    class_weights_list = []
    for c_name in CLASS_NAMES:
        c_count = train_counts.get(c_name, 1)
        w = n_train / (n_classes * c_count)
        class_weights_list.append(w)

    class_weights_tensor = torch.tensor(class_weights_list, dtype=torch.float32, device=device)
    print(f"Computed training-derived class weights:")
    for c_name, w in zip(CLASS_NAMES, class_weights_list):
        print(f"  {c_name:12}: {w:.4f} (count: {train_counts.get(c_name, 0)})")

    # 3. Create Datasets & Loaders
    train_ds = MangoDataset(train_df, DATASET_ROOT, transform=get_train_transforms())
    val_ds = MangoDataset(val_df, DATASET_ROOT, transform=get_eval_transforms())
    test_ds = MangoDataset(test_df, DATASET_ROOT, transform=get_eval_transforms())

    batch_size = 32
    train_loader = DataLoader(train_ds, batch_size=batch_size, shuffle=True, num_workers=0)
    val_loader = DataLoader(val_ds, batch_size=batch_size, shuffle=False, num_workers=0)
    test_loader = DataLoader(test_ds, batch_size=batch_size, shuffle=False, num_workers=0)

    # 4. Initialize Model
    print(f"Initializing MobileNetV3-Large with pretrained ImageNet weights...")
    model = build_mobilenet_v3_large(num_classes=n_classes, pretrained=True)
    model = model.to(device)

    criterion = nn.CrossEntropyLoss(weight=class_weights_tensor)

    # 5. Training Loop: 2-Stage Strategy
    # Stage 1: Warmup classifier head (epochs 1-4, lr=1e-3, AdamW)
    # Stage 2: Fine-tune upper blocks (epochs 5-10, lr=1e-4, CosineAnnealing)
    total_epochs = 10
    history = []
    best_val_macro_f1 = -1.0
    best_epoch = -1

    optimizer = torch.optim.AdamW(model.classifier.parameters(), lr=1e-3, weight_decay=1e-4)

    print("\n--- STAGE 1: CLASSIFIER HEAD TRAINING (EPOCHS 1-4) ---")
    for epoch in range(1, 5):
        ep_t0 = time.time()
        tr_loss, tr_acc, tr_p, tr_r, tr_f1 = run_epoch(model, train_loader, criterion, device, optimizer)
        val_loss, val_acc, val_p, val_r, val_f1 = run_epoch(model, val_loader, criterion, device, None)
        ep_time = time.time() - ep_t0

        ep_record = {
            "epoch": epoch,
            "stage": 1,
            "train_loss": float(tr_loss), "train_acc": float(tr_acc), "train_macro_f1": float(tr_f1),
            "val_loss": float(val_loss), "val_acc": float(val_acc),
            "val_macro_precision": float(val_p), "val_macro_recall": float(val_r), "val_macro_f1": float(val_f1),
            "epoch_time_sec": float(ep_time)
        }
        history.append(ep_record)

        print(f"Epoch [{epoch:02d}/{total_epochs:02d}] ({ep_time:.1f}s) | "
              f"Train Loss: {tr_loss:.4f}, Acc: {tr_acc*100:.2f}%, F1: {tr_f1:.4f} | "
              f"Val Loss: {val_loss:.4f}, Acc: {val_acc*100:.2f}%, F1: {val_f1:.4f}")

        if val_f1 > best_val_macro_f1:
            best_val_macro_f1 = val_f1
            best_epoch = epoch
            SAVED_MODELS_DIR.mkdir(parents=True, exist_ok=True)
            torch.save(model.state_dict(), BEST_CHECKPOINT_PATH)
            print(f"   >>> Saved new best model checkpoint (Val Macro-F1: {val_f1:.4f})")

    # Stage 2: Unfreeze top feature layers (features[13:])
    print("\n--- STAGE 2: FINE-TUNING UPPER BACKBONE (EPOCHS 5-10) ---")
    for param in model.features[13:].parameters():
        param.requires_grad = True

    trainable_params = [
        {"params": model.features[13:].parameters(), "lr": 1e-4},
        {"params": model.classifier.parameters(), "lr": 5e-4}
    ]
    optimizer = torch.optim.AdamW(trainable_params, weight_decay=1e-4)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=6, eta_min=1e-6)

    for epoch in range(5, total_epochs + 1):
        ep_t0 = time.time()
        tr_loss, tr_acc, tr_p, tr_r, tr_f1 = run_epoch(model, train_loader, criterion, device, optimizer)
        scheduler.step()
        val_loss, val_acc, val_p, val_r, val_f1 = run_epoch(model, val_loader, criterion, device, None)
        ep_time = time.time() - ep_t0

        ep_record = {
            "epoch": epoch,
            "stage": 2,
            "train_loss": float(tr_loss), "train_acc": float(tr_acc), "train_macro_f1": float(tr_f1),
            "val_loss": float(val_loss), "val_acc": float(val_acc),
            "val_macro_precision": float(val_p), "val_macro_recall": float(val_r), "val_macro_f1": float(val_f1),
            "epoch_time_sec": float(ep_time)
        }
        history.append(ep_record)

        print(f"Epoch [{epoch:02d}/{total_epochs:02d}] ({ep_time:.1f}s) | "
              f"Train Loss: {tr_loss:.4f}, Acc: {tr_acc*100:.2f}%, F1: {tr_f1:.4f} | "
              f"Val Loss: {val_loss:.4f}, Acc: {val_acc*100:.2f}%, F1: {val_f1:.4f}")

        if val_f1 > best_val_macro_f1:
            best_val_macro_f1 = val_f1
            best_epoch = epoch
            torch.save(model.state_dict(), BEST_CHECKPOINT_PATH)
            print(f"   >>> Saved new best model checkpoint (Val Macro-F1: {val_f1:.4f})")

    # 6. Load Best Checkpoint for Final Held-Out Test Evaluation
    print(f"\n=======================================================")
    print(f"Loading best checkpoint from Epoch {best_epoch} for Test Evaluation...")
    print(f"Checkpoint: {BEST_CHECKPOINT_PATH}")
    model.load_state_dict(torch.load(BEST_CHECKPOINT_PATH, map_location=device))

    test_results = evaluate_test_set(model, test_loader, device)

    # 7. Compute Checkpoint SHA-256
    with open(BEST_CHECKPOINT_PATH, "rb") as fp:
        checkpoint_sha256 = hashlib.sha256(fp.read()).hexdigest()

    # 8. Save Confusion Matrix Plot
    cm = np.array(test_results["confusion_matrix"])
    plt.figure(figsize=(8, 6))
    sns.heatmap(
        cm,
        annot=True,
        fmt="d",
        cmap="YlOrBr",
        xticklabels=CLASS_NAMES,
        yticklabels=CLASS_NAMES,
        cbar=True
    )
    plt.title(f"FreshIQ MobileNetV3 Mango — Test Confusion Matrix (Acc: {test_results['accuracy']*100:.2f}%)")
    plt.xlabel("Predicted Ripeness Stage")
    plt.ylabel("Ground Truth Stage")
    plt.tight_layout()
    plt.savefig(CONFUSION_MATRIX_PATH, dpi=300)
    plt.close()
    print(f"Saved confusion matrix plot: {CONFUSION_MATRIX_PATH}")

    # 9. Save Configuration JSON
    config_dict = {
        "model_name": "FreshIQ_MobileNetV3_Mango",
        "produce": "Mango",
        "variety": "White Chaunsa Late",
        "num_classes": n_classes,
        "class_mapping": CLASS_TO_IDX,
        "idx_to_class": IDX_TO_CLASS,
        "input_size": [3, 224, 224],
        "normalization": {"mean": IMAGENET_MEAN, "std": IMAGENET_STD},
        "pretrained_source": "torchvision.models.MobileNet_V3_Large_Weights.DEFAULT",
        "best_epoch": best_epoch,
        "total_epochs": total_epochs,
        "batch_size": batch_size,
        "seed": 42,
        "optimizer": "AdamW",
        "loss_function": "CrossEntropyLoss (Class-Weighted)",
        "class_weights": {c: float(w) for c, w in zip(CLASS_NAMES, class_weights_list)},
        "checkpoint_path": str(BEST_CHECKPOINT_PATH),
        "checkpoint_sha256": checkpoint_sha256,
        "total_train_time_sec": float(time.time() - start_time)
    }
    with open(CONFIG_PATH, "w", encoding="utf-8") as fp:
        json.dump(config_dict, fp, indent=2)
    print(f"Saved configuration: {CONFIG_PATH}")

    # 10. Save History JSON
    with open(HISTORY_PATH, "w", encoding="utf-8") as fp:
        json.dump(history, fp, indent=2)
    print(f"Saved training history: {HISTORY_PATH}")

    # 11. Save Test Evaluation JSON
    test_eval_dict = {
        "model_name": "FreshIQ_MobileNetV3_Mango",
        "checkpoint_sha256": checkpoint_sha256,
        "best_validation_epoch": best_epoch,
        "best_val_macro_f1": float(best_val_macro_f1),
        "held_out_test_metrics": test_results
    }
    with open(TEST_EVAL_PATH, "w", encoding="utf-8") as fp:
        json.dump(test_eval_dict, fp, indent=2)
    print(f"Saved test evaluation: {TEST_EVAL_PATH}")

    print(f"\n=== FINAL TEST EVALUATION COMPLETE ===")
    print(f"Test Accuracy:         {test_results['accuracy']*100:.2f}%")
    print(f"Within ±1 Stage Acc:   {test_results['within_plus_minus_1_stage_accuracy']*100:.2f}%")
    print(f"Ordinal Stage MAE:     {test_results['ordinal_stage_mae']:.4f} stages")
    print(f"Macro Precision:       {test_results['macro_precision']*100:.2f}%")
    print(f"Macro Recall:          {test_results['macro_recall']*100:.2f}%")
    print(f"Macro F1-Score:        {test_results['macro_f1']*100:.2f}%")
    print(f"Cohen's Kappa:         {test_results['cohens_kappa']:.4f}")
    print(f"Balanced Accuracy:     {test_results['balanced_accuracy']*100:.2f}%")
    print(f"Total Elapsed Time:    {(time.time() - start_time)/60:.1f} minutes")


if __name__ == "__main__":
    train_mango()
