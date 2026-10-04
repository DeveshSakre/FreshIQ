import torch
import torch.nn as nn
import torchvision.models as models
from torchvision.models import mobilenet_v3_small, MobileNet_V3_Small_Weights


def build_mobilenet_v3_small(
    num_classes: int = 5,
    pretrained: bool = True,
    freeze_backbone: bool = True
) -> nn.Module:
    """
    Constructs MobileNetV3-Small produce classifier.
    Replaces final linear layer in model.classifier with num_classes output layer.
    """
    weights = MobileNet_V3_Small_Weights.DEFAULT if pretrained else None
    model = mobilenet_v3_small(weights=weights)

    if freeze_backbone:
        for param in model.features.parameters():
            param.requires_grad = False

    in_features = model.classifier[-1].in_features
    model.classifier[-1] = nn.Linear(in_features, num_classes)

    return model
