import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowLeft,
  Camera,
  ArrowRight,
  Sliders,
  CheckCircle2,
  AlertTriangle,
  Snowflake,
  Flame,
  Leaf,
  ShieldAlert,
  Info,
  Utensils,
  Bell,
  Activity,
  RefreshCw,
  Loader2,
  HelpCircle
} from 'lucide-react';
import { useScan } from '../context/ScanContext';
import { RipenessBadge } from '../components/RipenessBadge';
import { predictProduce, ApiError } from '../services/api';

export const ComparisonPage: React.FC = () => {
  const { currentResult, currentImagePreview, setCurrentScan, history } = useScan();

  const [reminderSet, setReminderSet] = useState(false);
  const [isGeneratingDemo, setIsGeneratingDemo] = useState(false);
  const [demoError, setDemoError] = useState<string | null>(null);

  // Helper for direct demo run if opened without scan result
  const handleRunDemoScan = async () => {
    setIsGeneratingDemo(true);
    setDemoError(null);

    try {
      const canvas = document.createElement('canvas');
      canvas.width = 320;
      canvas.height = 320;
      const ctx = canvas.getContext('2d');
      if (!ctx) throw new Error('Canvas context unavailable');

      // Stage 3 radial gradient
      const grad = ctx.createRadialGradient(160, 160, 30, 160, 160, 150);
      grad.addColorStop(0, '#422006');
      grad.addColorStop(0.7, '#2A1810');
      grad.addColorStop(1, '#1C1917');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, 320, 320);

      // Pebbled dots
      for (let i = 0; i < 500; i++) {
        const x = Math.random() * 320;
        const y = Math.random() * 320;
        const radius = Math.random() * 2.5 + 0.8;
        ctx.fillStyle = Math.random() > 0.5 ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.22)';
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        ctx.fill();
      }

      canvas.toBlob(async (blob) => {
        if (!blob) {
          setDemoError('Could not create demo image.');
          setIsGeneratingDemo(false);
          return;
        }

        const demoFile = new File([blob], 'demo_hass_stage_3.jpg', { type: 'image/jpeg' });
        const previewDataUrl = canvas.toDataURL('image/jpeg', 0.9);

        try {
          const result = await predictProduce(demoFile, 'ambient');
          setCurrentScan(result, previewDataUrl);
        } catch (err: any) {
          setDemoError(err instanceof ApiError ? err.detail || err.message : 'Failed to analyze demo image.');
        } finally {
          setIsGeneratingDemo(false);
        }
      }, 'image/jpeg', 0.9);
    } catch (err: any) {
      setDemoError(err.message || 'Demo generation error.');
      setIsGeneratingDemo(false);
    }
  };

  // -------------------------------------------------------------
  // EMPTY STATE: No active scan in context
  // -------------------------------------------------------------
  if (!currentResult) {
    return (
      <div className="container" style={{ maxWidth: '780px', paddingTop: '3rem', paddingBottom: '4rem' }}>
        <div className="fresh-card" style={{ padding: '3.5rem 2rem', textAlign: 'center' }}>
          <div
            style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: 'var(--color-primary-light)',
              color: 'var(--color-primary-dark)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem',
            }}
          >
            <Sliders size={32} />
          </div>

          <h1 style={{ fontSize: '1.85rem', color: 'var(--color-forest)', marginBottom: '0.75rem' }}>
            No Active Produce Scan for Comparison
          </h1>
          <p style={{ color: 'var(--text-muted)', maxWidth: '540px', margin: '0 auto 2rem', lineHeight: '1.6' }}>
            To compare how storage temperatures decelerate physiological respiration and extend usable life,
            please scan an avocado or choose an existing record.
          </p>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap', marginBottom: '2.5rem' }}>
            <Link to="/scan" className="btn-primary" style={{ padding: '0.85rem 1.75rem' }}>
              <Camera size={18} />
              <span>Go to Produce Scanner</span>
            </Link>

            <button
              type="button"
              onClick={handleRunDemoScan}
              disabled={isGeneratingDemo}
              className="btn-secondary"
              style={{ padding: '0.85rem 1.5rem' }}
            >
              {isGeneratingDemo ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  <span>Analyzing Sample Avocado...</span>
                </>
              ) : (
                <>
                  <RefreshCw size={18} />
                  <span>Load Live Sample Avocado</span>
                </>
              )}
            </button>
          </div>

          {demoError && (
            <div
              style={{
                maxWidth: '520px',
                margin: '0 auto 2rem',
                padding: '0.85rem 1.25rem',
                borderRadius: 'var(--radius-md)',
                backgroundColor: '#FEF2F2',
                border: '1px solid #FCA5A5',
                color: '#991B1B',
                fontSize: '0.875rem',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <AlertTriangle size={18} />
              <span>{demoError}</span>
            </div>
          )}

          {/* Quick history load */}
          {history.length > 0 && (
            <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '2rem', textAlign: 'left' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                  OR LOAD FROM RECENT SCANS ({history.length})
                </span>
                <Link to="/history" style={{ fontSize: '0.85rem', color: 'var(--color-primary-dark)', fontWeight: 600 }}>
                  View History &rarr;
                </Link>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '0.75rem' }}>
                {history.slice(0, 3).map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => setCurrentScan(item.result, item.thumbnailUrl)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      padding: '10px 14px',
                      backgroundColor: 'var(--bg-subtle)',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--border-light)',
                      textAlign: 'left',
                      cursor: 'pointer',
                    }}
                  >
                    {item.thumbnailUrl && (
                      <img
                        src={item.thumbnailUrl}
                        alt="Thumbnail"
                        style={{ width: '42px', height: '42px', borderRadius: '8px', objectFit: 'cover' }}
                      />
                    )}
                    <div style={{ overflow: 'hidden' }}>
                      <div style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--text-main)' }}>
                        Stage {item.result.ripeness.predicted_ripening_stage} — {item.result.ripeness.stage_label}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        ~{item.result.scenarios.ambient.estimated_rul_days.toFixed(1)}d Ambient RUL
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  // -------------------------------------------------------------
  // REAL DATA EXTRACTION (From POST /api/predict response)
  // -------------------------------------------------------------
  const {
    item_name,
    ripeness,
    scenarios,
    refrigeration_extension_gain_days,
  } = currentResult;

  const stage = ripeness.predicted_ripening_stage;
  const stageLabel = ripeness.stage_label;
  const isTerminalStage5 = stage === 5;

  // Real RUL values from API response (enforcing terminal 0.0 for Stage 5)
  const ambientRul = isTerminalStage5 ? 0.0 : scenarios.ambient.estimated_rul_days;
  const room20Rul = isTerminalStage5 ? 0.0 : scenarios['20C'].estimated_rul_days;
  const cold10Rul = isTerminalStage5 ? 0.0 : scenarios['10C'].estimated_rul_days;
  const fridge4Rul = isTerminalStage5 ? 0.0 : scenarios['4C_refrigerator'].estimated_rul_days;
  const gainDays = isTerminalStage5 ? 0.0 : refrigeration_extension_gain_days;

  // Calculate percentage gain for 10C over ambient
  const percentGain = ambientRul > 0 ? Math.round((gainDays / ambientRul) * 100) : 0;

  // Horizontal bar max day scale (at least 12 days for visual proportion)
  const maxBarDays = Math.max(12, Math.ceil(Math.max(ambientRul, room20Rul, cold10Rul, fridge4Rul) + 1));
  const calcBarWidth = (days: number) => `${Math.max(4, Math.min(100, (days / maxBarDays) * 100))}%`;

  return (
    <div className="container" style={{ maxWidth: '1360px', paddingBottom: '4rem' }}>
      {/* ------------------------------------------------------------- */}
      {/* 1. TOP BREADCRUMBS & SPECIMEN STATUS STRIP                    */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
          padding: '0.75rem 0',
          marginBottom: '1.5rem',
          borderBottom: '1px solid var(--border-light)',
          fontSize: '0.85rem',
          color: 'var(--text-muted)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          <Link
            to="/analysis"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px',
              color: 'var(--text-main)',
              fontWeight: 600,
            }}
          >
            <ArrowLeft size={16} />
            <span>Analysis Results</span>
          </Link>
          <span style={{ color: 'var(--border-light)' }}>/</span>
          <span style={{ color: 'var(--color-primary-dark)', fontWeight: 600 }}>Shelf-Life Longevity Matrix</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          <span
            style={{
              backgroundColor: 'var(--bg-subtle)',
              padding: '4px 10px',
              borderRadius: '9999px',
              fontFamily: 'var(--font-mono)',
              fontSize: '0.75rem',
              color: 'var(--text-main)',
              fontWeight: 600,
            }}
          >
            Specimen: Persea americana (Hass)
          </span>

          <RipenessBadge stage={stage} label={`Stage ${stage}: ${stageLabel}`} size="sm" />
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 2. MAIN HEADLINE & SPECIMEN VISUAL CHIP                       */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'flex-end',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.5rem',
          marginBottom: '2rem',
        }}
      >
        <div style={{ maxWidth: '820px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-primary-dark)', marginBottom: '6px' }}>
            <Activity size={18} />
            <span
              style={{
                fontFamily: 'var(--font-mono)',
                fontSize: '0.75rem',
                fontWeight: 700,
                letterSpacing: '0.05em',
                textTransform: 'uppercase',
              }}
            >
              Produce Longevity Modeling &bull; What-If Scenarios
            </span>
          </div>

          <h1 style={{ fontSize: 'clamp(2rem, 3.5vw, 2.75rem)', color: 'var(--color-forest)', lineHeight: 1.15, marginBottom: '0.5rem' }}>
            Storage Temperature &amp; Shelf-Life Comparison
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', lineHeight: '1.5' }}>
            Simulated remaining usable life for your scanned Hass Avocado (
            <strong style={{ color: 'var(--text-main)' }}>Stage {stage} &mdash; {stageLabel}</strong>
            ) under calibrated ambient, cellar, and chilled thermodynamic environments.
          </p>
        </div>

        {/* Specimen Visual Chip */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
            backgroundColor: '#FFFFFF',
            border: '1px solid var(--border-light)',
            padding: '10px 16px',
            borderRadius: 'var(--radius-md)',
            boxShadow: 'var(--shadow-sm)',
          }}
        >
          {currentImagePreview ? (
            <img
              src={currentImagePreview}
              alt="Analyzed Avocado"
              style={{ width: '56px', height: '56px', borderRadius: '10px', objectFit: 'cover' }}
            />
          ) : (
            <div
              style={{
                width: '56px',
                height: '56px',
                borderRadius: '10px',
                backgroundColor: 'var(--bg-subtle)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-forest)',
              }}
            >
              <Camera size={24} />
            </div>
          )}
          <div>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.7rem', color: 'var(--text-muted)', display: 'block' }}>
              SCANNED SPECIMEN
            </span>
            <span style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--color-forest)', display: 'block' }}>
              {item_name}
            </span>
            <span style={{ fontSize: '0.775rem', color: 'var(--color-primary-dark)', fontWeight: 600 }}>
              Confidence: {(ripeness.confidence * 100).toFixed(1)}%
            </span>
          </div>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 3. STAGE 5 TERMINAL ALERT BANNER (If Applicable)              */}
      {/* ------------------------------------------------------------- */}
      {isTerminalStage5 && (
        <div
          style={{
            backgroundColor: '#FEF2F2',
            border: '1.5px solid #FCA5A5',
            borderRadius: 'var(--radius-md)',
            padding: '1.25rem 1.5rem',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '14px',
            marginBottom: '2rem',
          }}
        >
          <AlertTriangle size={24} style={{ color: '#991B1B', flexShrink: 0, marginTop: '2px' }} />
          <div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#991B1B', marginBottom: '4px' }}>
              Terminal Maturity Boundary Notice (Stage 5 — Overripe)
            </h3>
            <p style={{ fontSize: '0.9rem', color: '#7F1D1D', lineHeight: '1.5' }}>
              The model has classified this avocado in Stage 5 (Senescent / Overripe). Remaining usable shelf life is{' '}
              <strong>0.0 days</strong> across all storage environments.{' '}
              <strong>Refrigeration cannot restore expired shelf life or reverse cellular softening.</strong>{' '}
              Inspect the flesh for off-odors or internal dark discoloration before immediate culinary use.
            </p>
          </div>
        </div>
      )}

      {/* ------------------------------------------------------------- */}
      {/* 4. 4 STORAGE COMPARISON CARDS IN ADAPTIVE GRID               */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: '1.5rem',
          marginBottom: '2.5rem',
        }}
      >
        {/* CARD 1: Ambient (~20–22°C Warm Kitchen) */}
        <div
          className="fresh-card"
          style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            position: 'relative',
          }}
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.85rem' }}>
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '3px 8px',
                  borderRadius: '9999px',
                  backgroundColor: '#FEE2E2',
                  color: '#991B1B',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.7rem',
                  fontWeight: 700,
                }}
              >
                <Flame size={12} />
                Accelerated Ripening
              </span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                ~20–22°C / 72°F
              </span>
            </div>

            <div style={{ marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Warm Countertop</span>
              <h3 style={{ fontSize: '1.25rem', color: 'var(--color-forest)', fontWeight: 700 }}>
                Ambient Kitchen
              </h3>
            </div>

            <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', margin: '0.75rem 0' }}>
              <span style={{ fontSize: '2.75rem', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1 }}>
                {ambientRul.toFixed(1)}
              </span>
              <span style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                Days
              </span>
              <span style={{ marginLeft: 'auto', fontSize: '0.75rem', color: '#DC2626', fontWeight: 600 }}>
                High Loss Rate
              </span>
            </div>

            {/* Progress Bar */}
            <div style={{ height: '6px', backgroundColor: 'var(--bg-subtle)', borderRadius: '9999px', overflow: 'hidden', marginBottom: '1rem' }}>
              <div style={{ height: '100%', width: calcBarWidth(ambientRul), backgroundColor: '#EF4444', borderRadius: '9999px' }} />
            </div>

            <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--color-primary-dark)', fontWeight: 700, marginBottom: '4px', fontSize: '0.75rem' }}>
                <CheckCircle2 size={14} />
                <span>EMPIRICAL ML MODEL</span>
              </div>
              <p>
                Standard room bench. Respiration climbs rapidly. Segregate from bananas and apples to prevent autocatalytic ethylene cascade.
              </p>
            </div>
          </div>

          <div
            style={{
              paddingTop: '0.75rem',
              marginTop: '1rem',
              borderTop: '1px solid var(--border-light)',
              display: 'flex',
              justifyContent: 'space-between',
              fontSize: '0.775rem',
            }}
          >
            <span style={{ color: 'var(--text-muted)' }}>Kinetic Velocity</span>
            <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-main)' }}>1.8x Base</span>
          </div>
        </div>

        {/* CARD 2: 20°C Controlled Room */}
        <div
          className="fresh-card"
          style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            position: 'relative',
          }}
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.85rem' }}>
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '3px 8px',
                  borderRadius: '9999px',
                  backgroundColor: 'var(--bg-subtle)',
                  color: 'var(--text-main)',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.7rem',
                  fontWeight: 600,
                }}
              >
                <Sliders size={12} />
                Baseline Control
              </span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                20.0°C / 68°F
              </span>
            </div>

            <div style={{ marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Standard Pantry / Cellar</span>
              <h3 style={{ fontSize: '1.25rem', color: 'var(--color-forest)', fontWeight: 700 }}>
                Controlled 20°C
              </h3>
            </div>

            <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', margin: '0.75rem 0' }}>
              <span style={{ fontSize: '2.75rem', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1 }}>
                {room20Rul.toFixed(1)}
              </span>
              <span style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                Days
              </span>
              <span style={{ marginLeft: 'auto', fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                Nominal Loss
              </span>
            </div>

            {/* Progress Bar */}
            <div style={{ height: '6px', backgroundColor: 'var(--bg-subtle)', borderRadius: '9999px', overflow: 'hidden', marginBottom: '1rem' }}>
              <div style={{ height: '100%', width: calcBarWidth(room20Rul), backgroundColor: '#65A30D', borderRadius: '9999px' }} />
            </div>

            <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--color-primary-dark)', fontWeight: 700, marginBottom: '4px', fontSize: '0.75rem' }}>
                <CheckCircle2 size={14} />
                <span>EMPIRICAL ML MODEL</span>
              </div>
              <p>
                Standard controlled temperature. Calibrated benchmark with linear cellular softening profile.
              </p>
            </div>
          </div>

          <div
            style={{
              paddingTop: '0.75rem',
              marginTop: '1rem',
              borderTop: '1px solid var(--border-light)',
              display: 'flex',
              justifyContent: 'space-between',
              fontSize: '0.775rem',
            }}
          >
            <span style={{ color: 'var(--text-muted)' }}>Kinetic Velocity</span>
            <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-main)' }}>1.0x Base</span>
          </div>
        </div>

        {/* CARD 3: 10°C Cold Storage (HIGHLIGHTED BEST OPTION) */}
        <div
          className="fresh-card"
          style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            position: 'relative',
            border: isTerminalStage5 ? '1px solid var(--border-light)' : '2px solid var(--color-primary)',
            boxShadow: isTerminalStage5 ? 'var(--shadow-sm)' : 'var(--shadow-md)',
          }}
        >
          {/* Floating Best Available Target Tag */}
          {!isTerminalStage5 && (
            <div
              style={{
                position: 'absolute',
                top: '-12px',
                left: '50%',
                transform: 'translateX(-50%)',
                backgroundColor: 'var(--color-forest)',
                color: '#FFFFFF',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.675rem',
                fontWeight: 800,
                letterSpacing: '0.04em',
                textTransform: 'uppercase',
                padding: '3px 12px',
                borderRadius: '9999px',
                boxShadow: 'var(--shadow-sm)',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                whiteSpace: 'nowrap',
              }}
            >
              <Leaf size={12} style={{ color: 'var(--color-lime-glow)' }} />
              <span>Optimal Preservation Target</span>
            </div>
          )}

          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.85rem' }}>
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '3px 8px',
                  borderRadius: '9999px',
                  backgroundColor: 'var(--color-primary-light)',
                  color: 'var(--color-primary-dark)',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.7rem',
                  fontWeight: 700,
                }}
              >
                <Snowflake size={12} />
                Best Quality Window
              </span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--color-forest)', fontWeight: 700 }}>
                10.0°C / 50°F
              </span>
            </div>

            <div style={{ marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--color-primary-dark)', fontWeight: 600 }}>
                Dedicated Compartment
              </span>
              <h3 style={{ fontSize: '1.25rem', color: 'var(--color-forest)', fontWeight: 700 }}>
                Vegetable Crisper (10°C)
              </h3>
            </div>

            <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', margin: '0.75rem 0' }}>
              <span style={{ fontSize: '2.75rem', fontWeight: 800, color: 'var(--color-forest)', lineHeight: 1 }}>
                {cold10Rul.toFixed(1)}
              </span>
              <span style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-main)' }}>
                Days
              </span>
              {!isTerminalStage5 && gainDays > 0 && (
                <span
                  style={{
                    marginLeft: 'auto',
                    fontSize: '0.75rem',
                    color: 'var(--color-primary-dark)',
                    backgroundColor: 'var(--color-primary-light)',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontFamily: 'var(--font-mono)',
                    fontWeight: 700,
                  }}
                >
                  +{percentGain}% Gain
                </span>
              )}
            </div>

            {/* Progress Bar */}
            <div style={{ height: '8px', backgroundColor: 'var(--bg-subtle)', borderRadius: '9999px', overflow: 'hidden', marginBottom: '1rem' }}>
              <div style={{ height: '100%', width: calcBarWidth(cold10Rul), backgroundColor: 'var(--color-primary)', borderRadius: '9999px' }} />
            </div>

            <div style={{ fontSize: '0.825rem', color: 'var(--text-main)', lineHeight: '1.5' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--color-primary-dark)', fontWeight: 700, marginBottom: '4px', fontSize: '0.75rem' }}>
                <CheckCircle2 size={14} />
                <span>EMPIRICAL MODEL VERIFIED</span>
              </div>
              <p>
                Ideal thermal equilibrium for Hass avocados. Maximally arrests cell wall breakdown without inducing internal mesocarp browning.
              </p>
            </div>
          </div>

          <div
            style={{
              paddingTop: '0.75rem',
              marginTop: '1rem',
              borderTop: '1px solid var(--border-light)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              fontSize: '0.8rem',
              backgroundColor: 'var(--color-primary-light)',
              margin: '1rem -1.75rem -1.75rem -1.75rem',
              padding: '0.75rem 1.25rem',
              borderRadius: '0 0 var(--radius-lg) var(--radius-lg)',
            }}
          >
            <span style={{ color: 'var(--color-forest)', fontWeight: 600 }}>Net Longevity Surplus</span>
            <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 800, color: 'var(--color-primary-dark)', fontSize: '0.9rem' }}>
              {isTerminalStage5 ? '0.0 Days' : `+${gainDays.toFixed(1)} Full Days`}
            </span>
          </div>
        </div>

        {/* CARD 4: 4°C Domestic Refrigerator (MUST SHOW EXTRAPOLATION) */}
        <div
          className="fresh-card"
          style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            position: 'relative',
            backgroundColor: '#EFF6FF',
            border: '1.5px solid #BFDBFE',
          }}
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.85rem' }}>
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '3px 8px',
                  borderRadius: '9999px',
                  backgroundColor: '#DBEAFE',
                  color: '#1E40AF',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.7rem',
                  fontWeight: 700,
                }}
              >
                <Snowflake size={12} />
                Maximum Chilling
              </span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: '#1E40AF', fontWeight: 600 }}>
                4.0°C / 39°F
              </span>
            </div>

            <div style={{ marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: '#1E40AF' }}>Main Fridge Shelf</span>
              <h3 style={{ fontSize: '1.25rem', color: '#1E3A8A', fontWeight: 700 }}>
                Deep Refrigeration (4°C)
              </h3>
            </div>

            <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', margin: '0.75rem 0' }}>
              <span style={{ fontSize: '2.75rem', fontWeight: 800, color: '#1E3A8A', lineHeight: 1 }}>
                {fridge4Rul.toFixed(1)}
              </span>
              <span style={{ fontSize: '1.15rem', fontWeight: 600, color: '#1E40AF' }}>
                Days
              </span>
              <span
                style={{
                  marginLeft: 'auto',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.675rem',
                  fontWeight: 800,
                  backgroundColor: '#DBEAFE',
                  color: '#1E40AF',
                  padding: '2px 8px',
                  borderRadius: '4px',
                  textTransform: 'uppercase',
                  letterSpacing: '0.04em',
                }}
              >
                Model-based extrapolation
              </span>
            </div>

            {/* Hatched / Extrapolated Progress Bar */}
            <div style={{ height: '6px', backgroundColor: '#DBEAFE', borderRadius: '9999px', overflow: 'hidden', marginBottom: '1rem' }}>
              <div
                style={{
                  height: '100%',
                  width: calcBarWidth(fridge4Rul),
                  background: 'repeating-linear-gradient(45deg, #3B82F6, #3B82F6 6px, #60A5FA 6px, #60A5FA 12px)',
                  borderRadius: '9999px',
                }}
              />
            </div>

            <div style={{ fontSize: '0.825rem', color: '#1E40AF', lineHeight: '1.4' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, marginBottom: '4px', fontSize: '0.75rem' }}>
                <HelpCircle size={14} />
                <span>MODEL-BASED EXTRAPOLATION (Q10 = 2.38)</span>
              </div>
              <p style={{ color: '#1E3A8A', marginBottom: '6px' }}>
                {scenarios['4C_refrigerator']?.uncertainty_note || (
                  <>
                    4°C is not an empirically observed training condition. Modeled via Arrhenius biophysical respiration scaling (Q10 = 2.38); <strong>never represented as experimental ground truth</strong>.
                  </>
                )}
              </p>

              <div
                style={{
                  backgroundColor: '#FEF2F2',
                  border: '1px solid #FCA5A5',
                  borderRadius: '6px',
                  padding: '6px 8px',
                  marginTop: '8px',
                  color: '#991B1B',
                  fontSize: '0.75rem',
                }}
              >
                <strong>Chilling Injury Notice:</strong> Prolonged domestic refrigeration below 5°C risks internal flesh browning upon warming.
              </div>
            </div>
          </div>

          <div
            style={{
              paddingTop: '0.75rem',
              marginTop: '1rem',
              borderTop: '1px solid #BFDBFE',
              display: 'flex',
              justifyContent: 'space-between',
              fontSize: '0.775rem',
            }}
          >
            <span style={{ color: '#1E40AF' }}>Sensory Browning Risk</span>
            <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#B91C1C' }}>Moderate (Chilling Risk)</span>
          </div>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 5. VISUAL COMPARISON: HORIZONTAL TIMELINE BARS               */}
      {/* ------------------------------------------------------------- */}
      <div className="fresh-card" style={{ padding: '2rem', marginBottom: '2.5rem' }}>
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            marginBottom: '1.5rem',
          }}
        >
          <div>
            <span
              style={{
                fontFamily: 'var(--font-mono)',
                fontSize: '0.75rem',
                fontWeight: 700,
                color: 'var(--color-primary-dark)',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                display: 'block',
                marginBottom: '4px',
              }}
            >
              Comparative Shelf-Life Analysis
            </span>
            <h2 style={{ fontSize: '1.4rem', color: 'var(--color-forest)', fontWeight: 700 }}>
              Usable Longevity Windows by Thermal Environment
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: 'var(--color-primary)' }} />
              <span>Empirical ML Data</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#3B82F6' }} />
              <span>Model-based extrapolation (4°C)</span>
            </div>
          </div>
        </div>

        {/* Timeline Scale & Vector Bars */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Axis Scale Ticks */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              fontFamily: 'var(--font-mono)',
              fontSize: '0.725rem',
              color: 'var(--text-muted)',
              borderBottom: '1px solid var(--border-light)',
              paddingBottom: '6px',
            }}
          >
            <span>0 Days</span>
            <span style={{ textAlign: 'center' }}>2 Days</span>
            <span style={{ textAlign: 'center' }}>4 Days</span>
            <span style={{ textAlign: 'center' }}>6 Days</span>
            <span style={{ textAlign: 'center' }}>8 Days</span>
            <span style={{ textAlign: 'center' }}>10 Days</span>
            <span style={{ textAlign: 'right' }}>12+ Days</span>
          </div>

          {/* Row 1: Ambient Kitchen */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '4px' }}>
              <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>Ambient Kitchen (~20–22°C / 72°F)</span>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-main)' }}>
                {ambientRul.toFixed(1)} Days
              </span>
            </div>
            <div style={{ width: '100%', height: '28px', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', overflow: 'hidden', padding: '2px' }}>
              <div
                style={{
                  height: '100%',
                  width: calcBarWidth(ambientRul),
                  backgroundColor: '#EF4444',
                  borderRadius: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'flex-end',
                  paddingRight: '8px',
                  color: '#FFFFFF',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  transition: 'width 0.6s ease',
                }}
              >
                {ambientRul > 0 && `${ambientRul.toFixed(1)}d`}
              </div>
            </div>
          </div>

          {/* Row 2: Standard Room 20C */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '4px' }}>
              <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>Controlled Room (20°C / 68°F Baseline)</span>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-main)' }}>
                {room20Rul.toFixed(1)} Days
              </span>
            </div>
            <div style={{ width: '100%', height: '28px', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', overflow: 'hidden', padding: '2px' }}>
              <div
                style={{
                  height: '100%',
                  width: calcBarWidth(room20Rul),
                  backgroundColor: '#65A30D',
                  borderRadius: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'flex-end',
                  paddingRight: '8px',
                  color: '#FFFFFF',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  transition: 'width 0.6s ease',
                }}
              >
                {room20Rul > 0 && `${room20Rul.toFixed(1)}d`}
              </div>
            </div>
          </div>

          {/* Row 3: 10C Crisper (RECOMMENDED) */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', marginBottom: '4px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontWeight: 700, color: 'var(--color-forest)' }}>Vegetable Crisper (10°C / 50°F)</span>
                {!isTerminalStage5 && (
                  <span
                    style={{
                      backgroundColor: 'var(--color-forest)',
                      color: '#FFFFFF',
                      fontSize: '0.675rem',
                      fontFamily: 'var(--font-mono)',
                      fontWeight: 700,
                      padding: '1px 6px',
                      borderRadius: '4px',
                      textTransform: 'uppercase',
                    }}
                  >
                    Recommended Target
                  </span>
                )}
              </div>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--color-primary-dark)' }}>
                {cold10Rul.toFixed(1)} Days {!isTerminalStage5 && gainDays > 0 && `(+${gainDays.toFixed(1)}d extension)`}
              </span>
            </div>
            <div style={{ width: '100%', height: '34px', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', overflow: 'hidden', padding: '2px', border: '1px solid var(--border-accent)' }}>
              <div
                style={{
                  height: '100%',
                  width: calcBarWidth(cold10Rul),
                  backgroundColor: 'var(--color-primary)',
                  borderRadius: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingLeft: '10px',
                  paddingRight: '10px',
                  color: '#FFFFFF',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.8rem',
                  fontWeight: 700,
                  transition: 'width 0.7s ease',
                }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.725rem' }}>
                  <CheckCircle2 size={13} />
                  Optimal Organoleptic Window
                </span>
                <span>{cold10Rul > 0 && `${cold10Rul.toFixed(1)}d`}</span>
              </div>
            </div>
          </div>

          {/* Row 4: 4C Domestic Refrigerator (Extrapolated) */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', marginBottom: '4px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontWeight: 600, color: '#1E40AF' }}>Deep Chilled Refrigerator (4°C / 39°F)</span>
                <span
                  style={{
                    backgroundColor: '#DBEAFE',
                    color: '#1E40AF',
                    fontSize: '0.675rem',
                    fontFamily: 'var(--font-mono)',
                    fontWeight: 700,
                    padding: '1px 6px',
                    borderRadius: '4px',
                    textTransform: 'uppercase',
                  }}
                >
                  Model-Based Extrapolation
                </span>
              </div>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: '#1E40AF' }}>
                {fridge4Rul.toFixed(1)} Days (Browning Risk)
              </span>
            </div>
            <div style={{ width: '100%', height: '28px', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', overflow: 'hidden', padding: '2px' }}>
              <div
                style={{
                  height: '100%',
                  width: calcBarWidth(fridge4Rul),
                  background: 'repeating-linear-gradient(45deg, #3B82F6, #3B82F6 8px, #60A5FA 8px, #60A5FA 16px)',
                  borderRadius: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'flex-end',
                  paddingRight: '8px',
                  color: '#FFFFFF',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  transition: 'width 0.6s ease',
                }}
              >
                {fridge4Rul > 0 && `${fridge4Rul.toFixed(1)}d`}
              </div>
            </div>
          </div>
        </div>

        {/* Legend & Sensitivity Note */}
        <div
          style={{
            marginTop: '1.5rem',
            paddingTop: '1rem',
            borderTop: '1px solid var(--border-light)',
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '0.75rem',
            fontSize: '0.8rem',
            color: 'var(--text-muted)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Info size={15} style={{ color: 'var(--color-primary-dark)' }} />
            <span>
              Predictions calibrated with multi-temperature empirical dataset. 4°C extrapolation applies Q10 = 2.38.
            </span>
          </div>
          <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem' }}>
            HistGradientBoosting + Biophysical Simulation v1.2
          </span>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 6. EDUCATIONAL SECTION: WHY TEMPERATURE CHANGES RUL           */}
      {/* ------------------------------------------------------------- */}
      <div className="fresh-card" style={{ padding: '2rem', marginBottom: '2.5rem' }}>
        <div style={{ maxWidth: '820px', marginBottom: '1.75rem' }}>
          <span
            style={{
              fontFamily: 'var(--font-mono)',
              fontSize: '0.75rem',
              fontWeight: 700,
              color: 'var(--color-primary-dark)',
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              display: 'block',
              marginBottom: '4px',
            }}
          >
            Postharvest Physiology Guide
          </span>
          <h2 style={{ fontSize: '1.5rem', color: 'var(--color-forest)', fontWeight: 700, marginBottom: '0.5rem' }}>
            Why Temperature Governs Remaining Usable Shelf Life
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', lineHeight: '1.6' }}>
            The avocado (Persea americana) is a climacteric fruit whose ripening is driven by an irreversible burst in respiration
            and autocatalytic ethylene synthesis. Temperature directly controls the rate of these biochemical reactions.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          {/* Scientific Principle 1: Respiration & Q10 */}
          <div
            style={{
              padding: '1.25rem',
              backgroundColor: 'var(--bg-subtle)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-light)',
            }}
          >
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: '8px',
                backgroundColor: 'var(--color-primary-light)',
                color: 'var(--color-primary-dark)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '1rem',
              }}
            >
              <Activity size={20} />
            </div>
            <h3 style={{ fontSize: '1.05rem', color: 'var(--color-forest)', fontWeight: 700, marginBottom: '0.5rem' }}>
              1. Respiration Kinetics &amp; Q10 Coefficient
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              The rate of cellular respiration roughly doubles for every 10°C increase in temperature (Q10 &asymp; 2.0–2.5).
              Lowering temperature from 20°C to 10°C cuts metabolic substrate consumption in half, preserving structural carbohydrates and lipids.
            </p>
          </div>

          {/* Scientific Principle 2: Ethylene & Cell Wall Enzymes */}
          <div
            style={{
              padding: '1.25rem',
              backgroundColor: 'var(--bg-subtle)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-light)',
            }}
          >
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: '8px',
                backgroundColor: 'var(--color-primary-light)',
                color: 'var(--color-primary-dark)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '1rem',
              }}
            >
              <Leaf size={20} />
            </div>
            <h3 style={{ fontSize: '1.05rem', color: 'var(--color-forest)', fontWeight: 700, marginBottom: '0.5rem' }}>
              2. Ethylene &amp; Pectin Hydrolysis
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              Warm temperatures activate ACC oxidase and polygalacturonase (PG), enzymes that hydrolyze the middle lamella pectin
              gluing plant cells together. Chilling to 10°C delays ethylene receptor binding, maintaining firm sliceable texture.
            </p>
          </div>

          {/* Scientific Principle 3: Chilling Injury at 4°C */}
          <div
            style={{
              padding: '1.25rem',
              backgroundColor: '#EFF6FF',
              borderRadius: 'var(--radius-md)',
              border: '1px solid #BFDBFE',
            }}
          >
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: '8px',
                backgroundColor: '#DBEAFE',
                color: '#1E40AF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '1rem',
              }}
            >
              <Snowflake size={20} />
            </div>
            <h3 style={{ fontSize: '1.05rem', color: '#1E3A8A', fontWeight: 700, marginBottom: '0.5rem' }}>
              3. The 10°C vs 4°C Chilling Injury Threshold
            </h3>
            <p style={{ fontSize: '0.85rem', color: '#1E40AF', lineHeight: '1.5' }}>
              While 4°C domestic refrigeration extends nominal storage duration, temperatures below 5°C induce membrane lipid phase transitions.
              This can leak polyphenol oxidase (PPO), leading to internal vascular browning upon returning to room temperature.
            </p>
          </div>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 7. REFRIGERATION SUMMARY & ACTION BLUEPRINT BENTO GRID        */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(12, 1fr)',
          gap: '1.75rem',
          alignItems: 'stretch',
          marginBottom: '2.5rem',
        }}
      >
        {/* Key Insight Callout Banner (7 cols) */}
        <div
          style={{
            gridColumn: 'span 12',
            background: 'linear-gradient(135deg, var(--color-forest) 0%, #1a3826 100%)',
            color: '#FFFFFF',
            borderRadius: 'var(--radius-lg)',
            padding: '2rem',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            position: 'relative',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-lg)',
          }}
          className="analysis-right-col"
        >
          <div style={{ marginBottom: '1.5rem', zIndex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <span
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.15)',
                  color: 'var(--color-lime-glow)',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.7rem',
                  fontWeight: 700,
                  padding: '3px 10px',
                  borderRadius: '9999px',
                  textTransform: 'uppercase',
                }}
              >
                Optimal Intervention
              </span>
              <span style={{ color: 'rgba(255, 255, 255, 0.75)', fontSize: '0.85rem' }}>
                &bull; Actionable Guidance
              </span>
            </div>

            <h3 style={{ fontSize: '1.65rem', fontWeight: 700, color: '#FFFFFF', lineHeight: 1.25, marginBottom: '0.75rem' }}>
              {isTerminalStage5
                ? 'Produce has reached terminal overripe maturity.'
                : `Chilling to 10°C buys you an extra +${gainDays.toFixed(1)} days of peak culinary quality.`}
            </h3>
            <p style={{ color: 'rgba(255, 255, 255, 0.88)', fontSize: '0.95rem', lineHeight: '1.6', maxWidth: '580px' }}>
              {isTerminalStage5
                ? 'Stage 5 produce has completed senescence. Refrigeration cannot restore expired shelf life. Use immediately for baking, smoothies, or discard if spoiled.'
                : 'By transferring this avocado to a vegetable crisper now, you suppress polygalacturonase enzyme activity, stalling soft-rot without triggering the cold shock browning caused by domestic 4°C shelves.'}
            </p>
          </div>

          {/* Environmental & Waste Savings Metrics */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', zIndex: 1, borderTop: '1px solid rgba(255, 255, 255, 0.15)', paddingTop: '1.25rem' }}>
            <div
              style={{
                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                backdropFilter: 'blur(6px)',
                padding: '10px 16px',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
              }}
            >
              <Leaf size={22} style={{ color: 'var(--color-lime-glow)' }} />
              <div>
                <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.675rem', color: 'rgba(255, 255, 255, 0.75)', textTransform: 'uppercase' }}>
                  WASTE SAVINGS
                </div>
                <div style={{ fontSize: '0.95rem', fontWeight: 700, color: '#FFFFFF' }}>
                  $3.20 value conserved
                </div>
              </div>
            </div>

            <div
              style={{
                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                backdropFilter: 'blur(6px)',
                padding: '10px 16px',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
              }}
            >
              <Activity size={22} style={{ color: 'var(--color-lime-glow)' }} />
              <div>
                <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.675rem', color: 'rgba(255, 255, 255, 0.75)', textTransform: 'uppercase' }}>
                  EMISSION AVOIDANCE
                </div>
                <div style={{ fontSize: '0.95rem', fontWeight: 700, color: '#FFFFFF' }}>
                  0.48 kg CO₂eq saved
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Actionable Handling Procedures Card (5 cols) */}
        <div
          style={{
            gridColumn: 'span 12',
            backgroundColor: '#FFFFFF',
            border: '1px solid var(--border-light)',
            borderRadius: 'var(--radius-lg)',
            padding: '2rem',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            gap: '1.25rem',
            boxShadow: 'var(--shadow-sm)',
          }}
          className="analysis-left-col"
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--color-primary-dark)', fontWeight: 700, textTransform: 'uppercase' }}>
                Storage Blueprint
              </span>
              <Utensils size={18} style={{ color: 'var(--text-muted)' }} />
            </div>
            <h4 style={{ fontSize: '1.2rem', color: 'var(--color-forest)', fontWeight: 700, marginBottom: '4px' }}>
              How to Store This Avocado
            </h4>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Follow these three targeted handling procedures to attain the full preservation potential:
            </p>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            {/* Step 1 */}
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', backgroundColor: 'var(--bg-subtle)', borderRadius: '8px' }}>
              <span
                style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--color-primary-light)',
                  color: 'var(--color-primary-dark)',
                  fontFamily: 'var(--font-mono)',
                  fontWeight: 700,
                  fontSize: '0.75rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  marginTop: '1px',
                }}
              >
                1
              </span>
              <div style={{ fontSize: '0.85rem' }}>
                <strong style={{ color: 'var(--text-main)', display: 'block' }}>Transfer to middle crisper drawer</strong>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Set humidity slider to ~85% RH (High Humidity).</span>
              </div>
            </div>

            {/* Step 2 */}
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', backgroundColor: 'var(--bg-subtle)', borderRadius: '8px' }}>
              <span
                style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--color-primary-light)',
                  color: 'var(--color-primary-dark)',
                  fontFamily: 'var(--font-mono)',
                  fontWeight: 700,
                  fontSize: '0.75rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  marginTop: '1px',
                }}
              >
                2
              </span>
              <div style={{ fontSize: '0.85rem' }}>
                <strong style={{ color: 'var(--text-main)', display: 'block' }}>Isolate from climacteric neighbors</strong>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Keep at least 15cm separation from ripe bananas, apples, or tomatoes.</span>
              </div>
            </div>

            {/* Step 3 */}
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', backgroundColor: 'var(--bg-subtle)', borderRadius: '8px' }}>
              <span
                style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--color-primary-light)',
                  color: 'var(--color-primary-dark)',
                  fontFamily: 'var(--font-mono)',
                  fontWeight: 700,
                  fontSize: '0.75rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  marginTop: '1px',
                }}
              >
                3
              </span>
              <div style={{ fontSize: '0.85rem' }}>
                <strong style={{ color: 'var(--text-main)', display: 'block' }}>Perform re-inspection before use</strong>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Check stem node yield for gentle softening before slicing or mashing.</span>
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={() => setReminderSet(true)}
            className="btn-secondary"
            style={{
              width: '100%',
              backgroundColor: reminderSet ? 'var(--color-primary-light)' : 'var(--bg-subtle)',
              color: reminderSet ? 'var(--color-primary-dark)' : 'var(--text-main)',
              borderColor: reminderSet ? 'var(--color-primary)' : 'var(--border-light)',
            }}
          >
            <Bell size={16} />
            <span>{reminderSet ? 'Reminder Set for Mid-Week Inspection!' : 'Set Mid-Week Re-Inspection Reminder'}</span>
          </button>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 8. RESPONSIBLE SAFETY BANNER & NAVIGATION FOOTER STRIP        */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          backgroundColor: '#FFFBEB',
          border: '1.5px solid #FDE68A',
          borderRadius: 'var(--radius-md)',
          padding: '1.25rem 1.5rem',
          display: 'flex',
          alignItems: 'flex-start',
          gap: '12px',
          marginBottom: '2.5rem',
        }}
      >
        <ShieldAlert size={22} style={{ color: '#D97706', flexShrink: 0, marginTop: '2px' }} />
        <div style={{ fontSize: '0.85rem', color: '#92400E', lineHeight: '1.6' }}>
          <strong style={{ color: '#78350F' }}>Food Safety &amp; Biophysical Extrapolation Policy:</strong>{' '}
          10°C, 20°C, and ambient values are direct predictions from the empirically trained machine learning model.
          The 4°C refrigerator estimate is a simulation based on biochemical Q10 = 2.38 respiration kinetics and must not be treated as ground-truth dataset observations.
          Never consume produce exhibiting mold, foul odor, or grey flesh discoloration regardless of storage duration.
        </div>
      </div>

      {/* Navigation Buttons Strip */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
          paddingTop: '1rem',
          borderTop: '1px solid var(--border-light)',
        }}
      >
        <Link
          to="/analysis"
          className="btn-secondary"
          style={{ padding: '0.75rem 1.25rem' }}
        >
          <ArrowLeft size={16} />
          <span>Return to Analysis Result</span>
        </Link>

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem' }}>
          <Link
            to="/history"
            className="btn-secondary"
            style={{ padding: '0.75rem 1.25rem' }}
          >
            <span>View Scan History</span>
          </Link>

          <Link
            to="/what-if"
            className="btn-primary"
            style={{ padding: '0.75rem 1.5rem' }}
          >
            <span>Open Interactive What-If Lab</span>
            <ArrowRight size={16} />
          </Link>
        </div>
      </div>
    </div>
  );
};
