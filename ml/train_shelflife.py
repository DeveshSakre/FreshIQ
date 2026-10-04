import torch
import json
import time
from pathlib import Path
from typing import Dict, List, Tuple

import joblib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

from sklearn.ensemble import GradientBoostingRegressor, HistGradientBoostingRegressor
from sklearn.linear_model import RidgeCV
from sklearn.metrics import mean_absolute_error, mean_squared_error, median_absolute_error, r2_score

from ml.config import TrainingConfig
from ml.shelflife_data import extract_features_and_targets


# The deployable feature list available at inference time (NO ground-truth ripening stage!)
FEATURE_COLS = [
    "p1",
    "p2",
    "p3",
    "p4",
    "p5",
    "expected_stage",
    "confidence",
    "temperature_c",
    "is_cold_storage"
]
TARGET_COL = "actual_rul"


def evaluate_predictions(y_true: np.ndarray, y_pred: np.ndarray) -> Dict:
    # Post-process: remaining days cannot be negative
    y_pred_clipped = np.clip(y_pred, 0, None)

    mae = float(mean_absolute_error(y_true, y_pred_clipped))
    rmse = float(np.sqrt(mean_squared_error(y_true, y_pred_clipped)))
    r2 = float(r2_score(y_true, y_pred_clipped))
    bias = float(np.mean(y_pred_clipped - y_true))
    med_ae = float(median_absolute_error(y_true, y_pred_clipped))

    return {
        "mae": mae,
        "rmse": rmse,
        "r2": r2,
        "mean_prediction_error_bias": bias,
        "median_absolute_error": med_ae,
        "count": len(y_true)
    }


def train_and_evaluate_phase1b():
    config = TrainingConfig()
    config.set_seed()

    model_dir = Path("ml/saved_models")
    diag_dir = Path("ml/diagnostics")
    model_dir.mkdir(parents=True, exist_ok=True)
    diag_dir.mkdir(parents=True, exist_ok=True)

    print("=" * 65)
    print("FRESHIQ PHASE 1B: REMAINING USABLE LIFE (RUL) MODEL TRAINING")
    print("=" * 65)

    # 1. Extract / Load features
    df = extract_features_and_targets(config)

    # 2. Inspect censorship counts per split
    print("\n--- SPECIMEN CENSORSHIP AUDIT ---")
    spec_censored = df.groupby(["split", "specimen_id"])["is_censored"].first().reset_index()
    for split_name in ["train", "val", "test"]:
        sub_specs = spec_censored[spec_censored["split"] == split_name]
        n_total = len(sub_specs)
        n_uncensored = (sub_specs["is_censored"] == False).sum()
        n_censored = (sub_specs["is_censored"] == True).sum()
        print(f"[{split_name.upper():5s}] Total specimens: {n_total} | Uncensored (reached Stage 5): {n_uncensored} | Right-Censored: {n_censored}")

    # 3. Filter to uncensored specimens with verified Stage 5 ground-truth
    df_uncensored = df[df["is_censored"] == False].copy()

    train_df = df_uncensored[df_uncensored["split"] == "train"].copy()
    val_df = df_uncensored[df_uncensored["split"] == "val"].copy()
    test_df = df_uncensored[df_uncensored["split"] == "test"].copy()

    print(f"\nUncensored Image Rows -> Train: {len(train_df):,} | Val: {len(val_df):,} | Test: {len(test_df):,}")

    X_train = train_df[FEATURE_COLS].values
    y_train = train_df[TARGET_COL].values

    X_val = val_df[FEATURE_COLS].values
    y_val = val_df[TARGET_COL].values

    X_test = test_df[FEATURE_COLS].values
    y_test = test_df[TARGET_COL].values

    # 4. Define candidate models
    candidates = {
        "Ridge Regression": RidgeCV(alphas=np.logspace(-2, 3, 20)),
        "Gradient Boosting": GradientBoostingRegressor(
            n_estimators=150,
            learning_rate=0.05,
            max_depth=3,
            random_state=config.seed
        ),
        "HistGradientBoosting": HistGradientBoostingRegressor(
            max_iter=150,
            learning_rate=0.05,
            max_depth=4,
            random_state=config.seed
        )
    }

    comparison_results = {}
    fitted_models = {}

    print("\n--- MODEL COMPARISON (VALIDATION SET SELECTION) ---")
    for name, model in candidates.items():
        t0 = time.time()
        model.fit(X_train, y_train)
        fit_time = time.time() - t0

        val_preds = model.predict(X_val)
        train_preds = model.predict(X_train)

        val_metrics = evaluate_predictions(y_val, val_preds)
        train_metrics = evaluate_predictions(y_train, train_preds)

        comparison_results[name] = {
            "val_mae": val_metrics["mae"],
            "val_rmse": val_metrics["rmse"],
            "val_r2": val_metrics["r2"],
            "val_bias": val_metrics["mean_prediction_error_bias"],
            "train_mae": train_metrics["mae"],
            "train_rmse": train_metrics["rmse"],
            "train_r2": train_metrics["r2"],
            "fit_time_sec": fit_time
        }
        fitted_models[name] = model

        print(f"[{name}]")
        print(f"   Val MAE:  {val_metrics['mae']:.4f} days | Val RMSE: {val_metrics['rmse']:.4f} days | Val R2: {val_metrics['r2']:.4f}")
        print(f"   Train MAE:{train_metrics['mae']:.4f} days | Train R2: {train_metrics['r2']:.4f} | Fit time: {fit_time:.2f}s")

    # Select best model strictly by lowest Validation MAE
    best_model_name = min(comparison_results, key=lambda k: comparison_results[k]["val_mae"])
    best_model = fitted_models[best_model_name]

    print("\n" + "=" * 65)
    print(f"[SELECTED BEST MODEL] (BY VALIDATION MAE): {best_model_name}")
    print(f"Validation MAE: {comparison_results[best_model_name]['val_mae']:.4f} days")
    print("=" * 65)

    # 5. Final Evaluation on Untouched Test Set
    test_preds = np.clip(best_model.predict(X_test), 0, None)
    test_df["predicted_rul"] = test_preds
    test_df["error"] = test_preds - y_test
    test_df["abs_error"] = np.abs(test_preds - y_test)

    overall_test_metrics = evaluate_predictions(y_test, test_preds)

    print("\n--- UNTOUCHED TEST SET OVERALL METRICS ---")
    print(f"Test MAE:                 {overall_test_metrics['mae']:.4f} days")
    print(f"Test RMSE:                {overall_test_metrics['rmse']:.4f} days")
    print(f"Test R2 Score:            {overall_test_metrics['r2']:.4f}")
    print(f"Test Mean Bias (Error):   {overall_test_metrics['mean_prediction_error_bias']:+.4f} days")
    print(f"Test Median Abs Error:    {overall_test_metrics['median_absolute_error']:.4f} days")

    # 6. Stratified Performance by Ripening Stage
    print("\n--- PERFORMANCE STRATIFIED BY ACTUAL RIPENING STAGE ---")
    stage_breakdown = []
    for stage in range(1, 6):
        sub = test_df[test_df["actual_stage"] == stage]
        if len(sub) > 0:
            m = evaluate_predictions(sub[TARGET_COL].values, sub["predicted_rul"].values)
            stage_breakdown.append({
                "stage": stage,
                "label": config.stage_labels[stage],
                "count": len(sub),
                "actual_rul_mean": float(sub[TARGET_COL].mean()),
                "pred_rul_mean": float(sub["predicted_rul"].mean()),
                "mae": m["mae"],
                "rmse": m["rmse"],
                "r2": m["r2"],
                "bias": m["mean_prediction_error_bias"]
            })
    stage_df = pd.DataFrame(stage_breakdown)
    print(stage_df[["stage", "label", "count", "actual_rul_mean", "pred_rul_mean", "mae", "bias"]].to_string(index=False))

    # 7. Stratified Performance by Storage Group (T10 vs T20 vs Tam)
    print("\n--- PERFORMANCE STRATIFIED BY STORAGE CONDITION ---")
    storage_breakdown = []
    for sg in ["T10", "T20", "Tam"]:
        sub = test_df[test_df["storage_group"] == sg]
        if len(sub) > 0:
            m = evaluate_predictions(sub[TARGET_COL].values, sub["predicted_rul"].values)
            storage_breakdown.append({
                "storage_group": sg,
                "temperature": "10C (Cold Storage)" if sg == "T10" else "20C (Controlled Room)" if sg == "T20" else "Ambient (~20-22C)",
                "count": len(sub),
                "actual_rul_mean": float(sub[TARGET_COL].mean()),
                "pred_rul_mean": float(sub["predicted_rul"].mean()),
                "mae": m["mae"],
                "rmse": m["rmse"],
                "r2": m["r2"],
                "bias": m["mean_prediction_error_bias"]
            })
    storage_df = pd.DataFrame(storage_breakdown)
    print(storage_df[["storage_group", "temperature", "count", "actual_rul_mean", "pred_rul_mean", "mae", "bias"]].to_string(index=False))

    # 8. Trajectory Analysis across Test Specimens
    print("\n--- TRAJECTORY CONSISTENCY AUDIT ON TEST SPECIMENS ---")
    # Sample 5 distinct test specimens across storage conditions
    sample_specimens = ["T10_014", "T10_072", "T20_014", "T20_026", "Tam_021"]
    # Check if they exist in test uncensored
    available_specs = [s for s in sample_specimens if s in test_df["specimen_id"].values]
    if len(available_specs) < 5:
        available_specs += [s for s in test_df["specimen_id"].unique() if s not in available_specs][:5 - len(available_specs)]

    trajectory_records = []
    for spec_id in available_specs:
        sub = test_df[test_df["specimen_id"] == spec_id].sort_values("observation_day").drop_duplicates("observation_day")
        sg = sub["storage_group"].iloc[0]
        print(f"\nSpecimen: {spec_id} ({sg}):")
        for _, row in sub.iterrows():
            print(f"   Day {int(row['observation_day']):02d} | Actual Stage {int(row['actual_stage'])} | Pred Stage {int(row['predicted_stage'])} (exp={row['expected_stage']:.2f}) | Actual RUL: {row['actual_rul']:.1f}d | Pred RUL: {row['predicted_rul']:.1f}d")
            trajectory_records.append({
                "specimen_id": spec_id,
                "storage_group": sg,
                "day": int(row["observation_day"]),
                "actual_stage": int(row["actual_stage"]),
                "predicted_stage": int(row["predicted_stage"]),
                "expected_stage": float(row["expected_stage"]),
                "actual_rul": float(row["actual_rul"]),
                "predicted_rul": float(row["predicted_rul"])
            })

    # 9. Diagnostic Visualizations
    print("\nGenerating Phase 1B Diagnostic Plots...")

    # Plot 1: Predicted vs Actual RUL
    plt.figure(figsize=(7.5, 6.5))
    plt.scatter(test_df[TARGET_COL], test_df["predicted_rul"], alpha=0.35, color="#1f77b4", edgecolors="none")
    max_val = max(test_df[TARGET_COL].max(), test_df["predicted_rul"].max()) + 1
    plt.plot([0, max_val], [0, max_val], "r--", linewidth=2, label="Ideal Line (y = x)")
    plt.title(f"FreshIQ RUL Model - Predicted vs Actual ({best_model_name})")
    plt.xlabel("Ground-Truth Remaining Days (Until Stage 5)")
    plt.ylabel("Predicted Remaining Days")
    plt.xlim(0, max_val)
    plt.ylim(0, max_val)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()
    plt.savefig(diag_dir / "rul_predicted_vs_actual.png", dpi=300)
    plt.close()

    # Plot 2: Residuals Plot
    plt.figure(figsize=(8, 5))
    plt.scatter(test_df[TARGET_COL], test_df["error"], alpha=0.35, color="#ff7f0e", edgecolors="none")
    plt.axhline(0, color="red", linestyle="--", linewidth=2)
    plt.title(f"FreshIQ RUL Model Residuals ({best_model_name})")
    plt.xlabel("Ground-Truth Remaining Days")
    plt.ylabel("Residual (Predicted - Actual Days)")
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.tight_layout()
    plt.savefig(diag_dir / "rul_residuals_plot.png", dpi=300)
    plt.close()

    # Plot 3: Error Distribution Histogram
    plt.figure(figsize=(8, 5))
    sns.histplot(test_df["error"], kde=True, bins=35, color="#2ca02c")
    plt.axvline(0, color="red", linestyle="--", linewidth=2, label="Zero Error")
    plt.axvline(overall_test_metrics["mean_prediction_error_bias"], color="blue", linestyle=":", label=f"Mean Bias ({overall_test_metrics['mean_prediction_error_bias']:+.2f}d)")
    plt.title("FreshIQ Shelf-Life Prediction Error Distribution (Test Set)")
    plt.xlabel("Prediction Error (Days)")
    plt.ylabel("Frequency")
    plt.legend()
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.tight_layout()
    plt.savefig(diag_dir / "rul_error_distribution.png", dpi=300)
    plt.close()

    # Plot 4: Storage Condition Comparison Boxplot
    plt.figure(figsize=(8, 5.5))
    sns.boxplot(x="storage_group", y="abs_error", data=test_df, palette="Set2")
    plt.title("Absolute Error Across Storage Conditions (10C vs 20C vs Ambient)")
    plt.xlabel("Storage Condition")
    plt.ylabel("Absolute Error (Days)")
    plt.grid(axis="y", linestyle="--", alpha=0.5)
    plt.tight_layout()
    plt.savefig(diag_dir / "rul_storage_condition_comparison.png", dpi=300)
    plt.close()

    # Plot 5: Trajectory Specimens Plot
    plt.figure(figsize=(9, 5.5))
    palette = sns.color_palette("tab10", len(available_specs))
    for idx, spec_id in enumerate(available_specs):
        spec_data = [t for t in trajectory_records if t["specimen_id"] == spec_id]
        days = [t["day"] for t in spec_data]
        act = [t["actual_rul"] for t in spec_data]
        pred = [t["predicted_rul"] for t in spec_data]
        sg = spec_data[0]["storage_group"]
        col = palette[idx]
        plt.plot(days, act, "-", color=col, alpha=0.5, linewidth=1.5, label=f"{spec_id} ({sg}) Actual" if idx < 3 else None)
        plt.plot(days, pred, "o--", color=col, linewidth=2, label=f"{spec_id} ({sg}) Pred" if idx < 3 else None)

    plt.title("Remaining Shelf-Life Trajectory Tracking across Time")
    plt.xlabel("Observation Day")
    plt.ylabel("Remaining Usable Days")
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()
    plt.savefig(diag_dir / "rul_trajectory_specimens.png", dpi=300)
    plt.close()

    # 10. Save Artifacts
    saved_model_path = model_dir / "freshiq_shelflife_model.joblib"
    joblib.dump(best_model, saved_model_path)

    feature_config = {
        "features": FEATURE_COLS,
        "target": TARGET_COL,
        "terminal_event": "Stage 5 (Overripe)",
        "model_type": best_model_name,
        "supported_temperatures_c": [10.0, 20.0],
        "what_if_extrapolation_temperatures": {
            "4C_refrigerator": {
                "method": "Biophysical Arrhenius Extrapolation",
                "empirical_q10_factor": 2.38,
                "assumption": "Respiration rate decelerates following empirical Q10 observed between 10C and 20C."
            }
        }
    }
    with open(model_dir / "shelflife_feature_config.json", "w") as f:
        json.dump(feature_config, f, indent=2)

    with open(model_dir / "shelflife_model_comparison.json", "w") as f:
        json.dump(comparison_results, f, indent=2)

    test_eval_summary = {
        "selected_model": best_model_name,
        "overall_test_metrics": overall_test_metrics,
        "stage_breakdown": stage_breakdown,
        "storage_breakdown": storage_breakdown,
        "trajectory_sample_count": len(trajectory_records)
    }
    with open(model_dir / "shelflife_test_evaluation.json", "w") as f:
        json.dump(test_eval_summary, f, indent=2)

    print(f"\nSaved Trained Model to:       {saved_model_path.resolve()}")
    print(f"Saved Feature Config to:       {(model_dir / 'shelflife_feature_config.json').resolve()}")
    print(f"Saved Test Evaluation JSON to: {(model_dir / 'shelflife_test_evaluation.json').resolve()}")
    print(f"Saved Diagnostic Plots to:     {diag_dir.resolve()}")
    print("=" * 65)

    return best_model_name, comparison_results, overall_test_metrics, stage_breakdown, storage_breakdown, trajectory_records


if __name__ == "__main__":
    train_and_evaluate_phase1b()
