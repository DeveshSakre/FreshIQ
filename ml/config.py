import os
import random
from dataclasses import dataclass, field
from pathlib import Path
import numpy as np
import torch

@dataclass
class TrainingConfig:
    data_root: Path = Path("Avocado Ripening Dataset")
    manifest_dir: Path = Path("freshiq_manifests")
    model_dir: Path = Path("ml/saved_models")

    train_csv: Path = field(default_factory=lambda: Path("freshiq_manifests/train.csv"))
    val_csv: Path = field(default_factory=lambda: Path("freshiq_manifests/validation.csv"))
    test_csv: Path = field(default_factory=lambda: Path("freshiq_manifests/test.csv"))

    num_classes: int = 5
    image_size: int = 224
    batch_size: int = 32
    learning_rate: float = 1e-3
    weight_decay: float = 1e-4
    num_epochs: int = 12
    num_workers: int = 0
    seed: int = 42

    device: str = field(default_factory=lambda: "cuda" if torch.cuda.is_available() else "cpu")
    best_model_filename: str = "freshiq_mobilenetv3_avocado_best.pth"

    stage_labels: dict = field(default_factory=lambda: {
        1: "Stage 1 — Underripe",
        2: "Stage 2 — Breaking",
        3: "Stage 3 — Ripe First Stage",
        4: "Stage 4 — Ripe Second Stage",
        5: "Stage 5 — Overripe"
    })

    class_to_idx: dict = field(default_factory=lambda: {
        "Stage 1 — Underripe": 0,
        "Stage 2 — Breaking": 1,
        "Stage 3 — Ripe First Stage": 2,
        "Stage 4 — Ripe Second Stage": 3,
        "Stage 5 — Overripe": 4
    })

    imagenet_mean: list = field(default_factory=lambda: [0.485, 0.456, 0.406])
    imagenet_std: list = field(default_factory=lambda: [0.229, 0.224, 0.225])

    def set_seed(self):
        random.seed(self.seed)
        np.random.seed(self.seed)
        torch.manual_seed(self.seed)
        if torch.cuda.is_available():
            torch.cuda.manual_seed_all(self.seed)

    def get_best_model_path(self) -> Path:
        self.model_dir.mkdir(parents=True, exist_ok=True)
        return self.model_dir / self.best_model_filename
