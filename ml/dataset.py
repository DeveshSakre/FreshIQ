from pathlib import Path
from typing import Dict, List, Tuple

import pandas as pd
import numpy as np
from PIL import Image

import torch
from torch.utils.data import Dataset, DataLoader
import torchvision.transforms as T

from ml.config import TrainingConfig


class AvocadoDataset(Dataset):
    """
    PyTorch Dataset for Hass Avocado Ripening photos.
    Reads images on demand from image filenames and returns (image_tensor, label_idx, specimen_id).
    """

    def __init__(self, df: pd.DataFrame, data_root: Path, transform=None):
        self.df = df.reset_index(drop=True).copy()
        self.data_root = Path(data_root)
        self.transform = transform

    def __len__(self) -> int:
        return len(self.df)

    def __getitem__(self, idx: int) -> Tuple[torch.Tensor, int, str]:
        row = self.df.iloc[idx]
        filename = str(row["filename"])
        if not filename.endswith(".jpg"):
            filename += ".jpg"

        image_path = self.data_root / filename
        image = Image.open(image_path).convert("RGB")

        if self.transform:
            image = self.transform(image)

        # Map ripening_stage (1..5) to class index (0..4)
        label = int(row["ripening_stage"]) - 1
        specimen_id = str(row["specimen_id"])

        return image, label, specimen_id


def get_train_transforms(image_size: int = 224, mean: list = None, std: list = None):
    if mean is None:
        mean = [0.485, 0.456, 0.406]
    if std is None:
        std = [0.229, 0.224, 0.225]

    return T.Compose([
        T.Resize((image_size, image_size)),
        T.RandomHorizontalFlip(p=0.5),
        T.RandomRotation(degrees=10),
        T.ColorJitter(
            brightness=0.15,
            contrast=0.15,
            saturation=0.10,
            hue=0.02
        ),
        T.ToTensor(),
        T.Normalize(mean=mean, std=std)
    ])


def get_eval_transforms(image_size: int = 224, mean: list = None, std: list = None):
    if mean is None:
        mean = [0.485, 0.456, 0.406]
    if std is None:
        std = [0.229, 0.224, 0.225]

    return T.Compose([
        T.Resize((image_size, image_size)),
        T.ToTensor(),
        T.Normalize(mean=mean, std=std)
    ])


def verify_and_load_splits(config: TrainingConfig) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    """
    Automated dataset verification before training:
    - Verifies image files exist on disk
    - Verifies ripening_stage labels are valid (1..5)
    - Verifies specimen IDs exist
    - Asserts no specimen ID appears in more than one split (leakage check)
    - Reports image, specimen, and class distribution counts
    """
    data_root = Path(config.data_root)

    print("=" * 60)
    print("FRESHIQ DATASET VERIFICATION & PREPARATION")
    print("=" * 60)

    train_raw = pd.read_csv(config.train_csv)
    val_raw = pd.read_csv(config.val_csv)
    test_raw = pd.read_csv(config.test_csv)

    cleaned_splits = []

    for name, df in [("Train", train_raw), ("Validation", val_raw), ("Test", test_raw)]:
        # 1. Label validity check
        invalid_labels = df[~df["ripening_stage"].isin([1, 2, 3, 4, 5])]
        if len(invalid_labels) > 0:
            raise ValueError(f"Found {len(invalid_labels)} invalid ripening stage labels in {name} split!")

        # 2. Specimen ID existence check
        null_specs = df["specimen_id"].isna().sum()
        if null_specs > 0:
            raise ValueError(f"Found {null_specs} null specimen IDs in {name} split!")

        # 3. Image file path existence check
        valid_rows = []
        missing_count = 0

        for idx, row in df.iterrows():
            fname = str(row["filename"])
            if not fname.endswith(".jpg"):
                fname += ".jpg"
            img_path = data_root / fname
            if img_path.exists():
                valid_rows.append(row)
            else:
                missing_count += 1

        cleaned_df = pd.DataFrame(valid_rows).reset_index(drop=True)
        cleaned_splits.append(cleaned_df)

        print(f"[{name:10s}] Manifest rows: {len(df):,d} | Missing JPGs: {missing_count:d} | Valid images: {len(cleaned_df):,d} | Specimens: {cleaned_df['specimen_id'].nunique():d}")

    train_df, val_df, test_df = cleaned_splits

    # 4. Leakage Verification (Specimen Disjointness)
    train_specs = set(train_df["specimen_id"])
    val_specs = set(val_df["specimen_id"])
    test_specs = set(test_df["specimen_id"])

    assert train_specs.isdisjoint(val_specs), "CRITICAL DATA LEAKAGE: Specimen overlap between Train and Validation!"
    assert train_specs.isdisjoint(test_specs), "CRITICAL DATA LEAKAGE: Specimen overlap between Train and Test!"
    assert val_specs.isdisjoint(test_specs), "CRITICAL DATA LEAKAGE: Specimen overlap between Validation and Test!"

    print("\n[OK] SPECIMEN LEAKAGE CHECK PASSED: 0 specimen overlap across splits.")

    print("\nClass distribution (Valid images):")
    dist_df = pd.DataFrame({
        "Train": train_df["ripening_stage"].value_counts().sort_index(),
        "Validation": val_df["ripening_stage"].value_counts().sort_index(),
        "Test": test_df["ripening_stage"].value_counts().sort_index()
    })
    dist_df["Total"] = dist_df.sum(axis=1)
    print(dist_df)
    print("=" * 60)

    return train_df, val_df, test_df


def compute_class_weights(train_df: pd.DataFrame, num_classes: int = 5, device: str = "cpu") -> torch.Tensor:
    """
    Computes class weights based ONLY on training set class distribution:
    weight[i] = N_total / (num_classes * N_class_i)
    """
    stage_counts = train_df["ripening_stage"].value_counts().sort_index()
    counts = np.array([stage_counts.get(i, 0) for i in range(1, num_classes + 1)], dtype=np.float32)

    total_samples = counts.sum()
    weights = total_samples / (num_classes * counts)

    print(f"\nCalculated Training Class Weights: {np.round(weights, 4)}")
    return torch.tensor(weights, dtype=torch.float32, device=device)
