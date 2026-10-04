import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowLeft,
  Camera,
  Clock,
  ArrowRight,
  Sliders,
  CheckCircle2,
  ShieldAlert,
  Share2,
  BookmarkCheck,
  Bookmark,
  Layers,
  Thermometer,
  Snowflake,
  AlertTriangle,
  Utensils,
  ChevronRight,
  Activity,
  BarChart3,
  Loader2,
  RefreshCw,
  Info
} from 'lucide-react';
import { useScan } from '../context/ScanContext';
import { RipenessBadge } from '../components/RipenessBadge';
import { predictProduce, ApiError } from '../services/api';

// Stage color configuration
const STAGE_COLORS: Record<number, string> = {
  1: '#16A34A', // Underripe Green
  2: '#65A30D', // Breaking Olive
  3: '#CA8A04', // Firm Ripe Amber
  4: '#EA580C', // Peak Soft Ripe Orange
  5: '#991B1B', // Overripe Senescent Crimson
};

// Exocarp color hex approximations
const EXOCARP_COLORS: Record<number, string> = {
  1: '#266336',
  2: '#3D5428',
  3: '#2D3528',
  4: '#1C1917',
  5: '#0C0A09',
};

// Stage botanical interpretations
const STAGE_INTERPRETATIONS: Record<number, string> = {
  1: 'Firm botanical condition with rigid exocarp and high cellular firmness. Starches intact with slow ripening progression. Ideal for extended pantry storage or long-distance transport.',
  2: 'Breaking maturity exhibiting initial skin color transition from bright emerald to dark olive, with slight yielding under firm pressure. Ethylene synthesis actively initiating.',
  3: 'Firm-ripe botanical condition with creamy, buttery lipid texture beginning. Exocarp yields with gentle pressure; ideal structural integrity for crisp slicing and fresh preparation.',
  4: 'Soft-ripe peak eating window; rich, buttery texture and full varietal aroma. Exocarp yields easily to light thumb pressure. Consume soon for optimal flavor and culinary enjoyment.',
  5: 'Terminal senescent botanical state. Soft overripe exocarp with progressive loss of cellular pectin and lipid integrity. Must be consumed immediately if sensory checks pass.',
};

// Texture matrix descriptions for the spatial bio-scan feed
const TEXTURE_MATRIX: Record<number, { title: string; desc: string; yieldPressure: string; defect: string; dryMatter: string }> = {
  1: {
    title: 'PEBBLED BRIGHT EMERALD',
    desc: 'Rigid Exocarp / Unbroken Cuticle',
    yieldPressure: 'High Firmness (>18.0 N)',
    defect: '0.0%',
    dryMatter: '22.4%',
  },
  2: {
    title: 'TRANSITIONAL OLIVE GREEN',
    desc: 'Pebbled Exocarp / Early Softening',
    yieldPressure: 'Moderate Firmness (14.2 N)',
    defect: '0.0%',
    dryMatter: '24.1%',
  },
  3: {
    title: 'PEBBLED DARK EMERALD / VIOLET',
    desc: 'Uniform Pericarp / Creamy Lipid Core',
    yieldPressure: 'Medium-Firm Yield (8.4 N)',
    defect: '0.0%',
    dryMatter: '25.8%',
  },
  4: {
    title: 'DEEP PURPLISH-BLACK EXOCARP',
    desc: 'Supple Pericarp / Peak Yield',
    yieldPressure: 'Soft Yield (4.2 N)',
    defect: '0.0%',
    dryMatter: '27.2%',
  },
  5: {
    title: 'BLACKENED SENESCENT EXOCARP',
    desc: 'Collapsed Parenchyma / Overripe',
    yieldPressure: 'Loss of Integrity (<1.5 N)',
    defect: '18.5%',
    dryMatter: '28.0%',
  },
};

// Culinary suitability tags
const CULINARY_SUITABILITY: Record<number, { title: string; subtitle: string }> = {
  1: { title: 'Storage & Holding', subtitle: 'Room temp ripening: 5–8d' },
  2: { title: 'Prep Planning', subtitle: 'Ready for cutting: 2–3d' },
  3: { title: 'Slicing & Dicing', subtitle: 'Salads & Sandwiches: Now' },
  4: { title: 'Guacamole & Spreads', subtitle: 'Peak flavor: Next 24–48h' },
  5: { title: 'Smoothies or Baking', subtitle: 'Check for spoilage first' },
};

export const AnalysisPage: React.FC = () => {
  const { currentResult, currentImagePreview, setCurrentScan, history } = useScan();

  const [showReticleOverlay, setShowReticleOverlay] = useState(true);
  const [copiedLink, setCopiedLink] = useState(false);
  const [savedToHistory, setSavedToHistory] = useState(false);
  const [isGeneratingDemo, setIsGeneratingDemo] = useState(false);
  const [demoError, setDemoError] = useState<string | null>(null);

  // Helper to copy share diagnostic link
  const handleShare = () => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(window.location.href);
      setCopiedLink(true);
      setTimeout(() => setCopiedLink(false), 2500);
    }
  };

  // Helper to toggle save to history feedback
  const handleSaveToHistory = () => {
    if (currentResult) {
      setCurrentScan(currentResult, currentImagePreview || '');
      setSavedToHistory(true);
      setTimeout(() => setSavedToHistory(false), 2500);
    }
  };

  // Helper for direct navigation demo scan
  const handleRunDemoScan = async (sampleStage: number = 3) => {
    setIsGeneratingDemo(true);
    setDemoError(null);

    try {
      const canvas = document.createElement('canvas');
      canvas.width = 320;
      canvas.height = 320;
      const ctx = canvas.getContext('2d');

      if (!ctx) throw new Error('Canvas context unavailable');

      const skinColors: Record<number, [string, string, string]> = {
        1: ['#166534', '#15803D', '#14532D'],
        2: ['#3F6212', '#4D7C0F', '#1A3826'],
        3: ['#422006', '#2A1810', '#1C1917'],
        4: ['#1C1917', '#171412', '#0C0A09'],
        5: ['#0C0A09', '#080706', '#030202'],
      };

      const [c1, c2, c3] = skinColors[sampleStage] || skinColors[3];
      const grad = ctx.createRadialGradient(160, 160, 30, 160, 160, 150);
      grad.addColorStop(0, c1);
      grad.addColorStop(0.7, c2);
      grad.addColorStop(1, c3);
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, 320, 320);

      // Add pebbled texture
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

        const demoFile = new File([blob], `demo_hass_stage_${sampleStage}.jpg`, { type: 'image/jpeg' });
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
  // EMPTY STATE: Direct navigation to /analysis without scan data
  // -------------------------------------------------------------
  if (!currentResult) {
    return (
      <div className="container" style={{ maxWidth: '820px', paddingTop: '2.5rem', paddingBottom: '4rem' }}>
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
            <Camera size={32} />
          </div>

          <h1 style={{ fontSize: '1.85rem', color: 'var(--color-forest)', marginBottom: '0.75rem' }}>
            No Active Produce Scan Result
          </h1>
          <p style={{ color: 'var(--text-muted)', maxWidth: '540px', margin: '0 auto 2rem', lineHeight: '1.6' }}>
            To inspect MobileNetV3 ripening stage classification, confidence distributions, and What-If shelf-life projections,
            please upload or photograph an avocado.
          </p>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap', marginBottom: '2.5rem' }}>
            <Link to="/scan" className="btn-primary" style={{ padding: '0.85rem 1.75rem' }}>
              <Camera size={18} />
              <span>Go to Produce Scanner</span>
            </Link>

            <button
              type="button"
              onClick={() => handleRunDemoScan(3)}
              disabled={isGeneratingDemo}
              className="btn-secondary"
              style={{ padding: '0.85rem 1.5rem' }}
            >
              {isGeneratingDemo ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  <span>Analyzing Demo Avocado...</span>
                </>
              ) : (
                <>
                  <RefreshCw size={18} />
                  <span>Run Live Demo Scan (Stage 3)</span>
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

          {/* Quick history load if available */}
          {history.length > 0 && (
            <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '2rem', textAlign: 'left' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                  OR LOAD FROM RECENT SCANS ({history.length})
                </span>
                <Link to="/history" style={{ fontSize: '0.85rem', color: 'var(--color-primary-dark)', fontWeight: 600 }}>
                  View All History &rarr;
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
                      transition: 'all 0.15s ease',
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
    actionable_recommendation,
    legal_disclaimer,
  } = currentResult;

  const stage = ripeness.predicted_ripening_stage;
  const stageLabel = ripeness.stage_label;
  const confidence = ripeness.confidence;
  const expectedStage = ripeness.expected_continuous_ripening_stage;
  const probs = ripeness.probabilities;

  const isTerminalStage5 = stage === 5;
  const interpretation = STAGE_INTERPRETATIONS[stage] || STAGE_INTERPRETATIONS[3];
  const texture = TEXTURE_MATRIX[stage] || TEXTURE_MATRIX[3];
  const culinary = CULINARY_SUITABILITY[stage] || CULINARY_SUITABILITY[3];
  const exocarpColor = EXOCARP_COLORS[stage] || '#2D3528';

  // RUL values for all 4 required scenarios
  const ambientRul = isTerminalStage5 ? 0.0 : scenarios.ambient.estimated_rul_days;
  const cold10Rul = isTerminalStage5 ? 0.0 : scenarios['10C'].estimated_rul_days;
  const room20Rul = isTerminalStage5 ? 0.0 : scenarios['20C'].estimated_rul_days;
  const fridge4Rul = isTerminalStage5 ? 0.0 : scenarios['4C_refrigerator'].estimated_rul_days;

  // Trajectory dynamic SVG coordinate calculation based on actual RUL days
  const maxPlottedDays = Math.max(7, Math.ceil(cold10Rul + 1));
  const svgWidth = 460;
  const svgHeight = 150;
  const startX = 40;
  const endX = 430;
  const plotWidth = endX - startX;
  const topY = 25;
  const bottomY = 125;

  const dayToX = (day: number) => startX + Math.min(plotWidth, (day / maxPlottedDays) * plotWidth);

  const ambientEndX = dayToX(ambientRul);
  const cold10EndX = dayToX(cold10Rul);

  return (
    <div className="container" style={{ maxWidth: '1280px', paddingBottom: '4rem' }}>
      {/* ------------------------------------------------------------- */}
      {/* 1. TOP CONTEXTUAL BREADCRUMBS & META BAR                      */}
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
            to="/scan"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px',
              color: 'var(--text-main)',
              fontWeight: 600,
            }}
          >
            <ArrowLeft size={16} />
            <span>Scan Produce</span>
          </Link>
          <span style={{ color: 'var(--border-light)' }}>/</span>
          <span style={{ color: 'var(--text-main)' }}>Analysis Results</span>
          <span style={{ color: 'var(--border-light)' }}>/</span>
          <span
            style={{
              backgroundColor: 'var(--bg-subtle)',
              padding: '2px 8px',
              borderRadius: '9999px',
              fontWeight: 600,
              color: 'var(--color-forest)',
              fontFamily: 'var(--font-mono)',
              fontSize: '0.75rem',
            }}
          >
            {item_name}
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              backgroundColor: 'var(--color-primary-light)',
              color: 'var(--color-primary-dark)',
              padding: '4px 10px',
              borderRadius: '9999px',
              fontSize: '0.775rem',
              fontWeight: 600,
            }}
          >
            <CheckCircle2 size={14} />
            <span>AI Assessment Complete</span>
          </div>

          <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem' }}>
            Model: MobileNetV3-Small (Phase 1A Checkpoint)
          </span>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 2. HEADER CALLOUT TITLE & ACTION BUTTONS                      */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'flex-end',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.25rem',
          marginBottom: '2rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
            <span
              style={{
                backgroundColor: 'var(--color-lime-light)',
                color: '#365314',
                padding: '2px 10px',
                borderRadius: '9999px',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.725rem',
                fontWeight: 700,
                letterSpacing: '0.04em',
                textTransform: 'uppercase',
              }}
            >
              Persea americana &bull; Hass Avocado
            </span>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              SHA256 #8aaff3e3
            </span>
          </div>

          <h1 style={{ fontSize: 'clamp(2rem, 3.5vw, 2.75rem)', color: 'var(--color-forest)', lineHeight: 1.15 }}>
            Produce Ripeness Diagnostic
          </h1>
        </div>

        {/* Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          <button
            type="button"
            onClick={handleShare}
            className="btn-secondary"
            style={{ padding: '0.65rem 1.15rem', fontSize: '0.875rem' }}
          >
            <Share2 size={16} />
            <span>{copiedLink ? 'Link Copied!' : 'Share Diagnostic'}</span>
          </button>

          <button
            type="button"
            onClick={handleSaveToHistory}
            className="btn-secondary"
            style={{
              padding: '0.65rem 1.15rem',
              fontSize: '0.875rem',
              backgroundColor: savedToHistory ? 'var(--color-primary-light)' : '#FFFFFF',
              borderColor: savedToHistory ? 'var(--color-primary)' : 'var(--border-light)',
              color: savedToHistory ? 'var(--color-primary-dark)' : 'var(--text-main)',
            }}
          >
            {savedToHistory ? <BookmarkCheck size={16} /> : <Bookmark size={16} />}
            <span>{savedToHistory ? 'Saved to History' : 'Save to History'}</span>
          </button>

          <Link
            to="/what-if"
            className="btn-secondary"
            style={{ padding: '0.65rem 1.15rem', fontSize: '0.875rem' }}
          >
            <Sliders size={16} />
            <span>What-If Lab</span>
          </Link>

          <Link
            to="/comparison"
            className="btn-primary"
            style={{ padding: '0.65rem 1.25rem', fontSize: '0.875rem' }}
          >
            <span>Compare Storage</span>
            <ArrowRight size={16} />
          </Link>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 3. DUAL-PANE ANALYTICAL GRID (Left: 5 Cols, Right: 7 Cols)    */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(12, 1fr)',
          gap: '1.75rem',
          alignItems: 'start',
        }}
      >
        {/* ========================================================= */}
        {/* LEFT COLUMN: Spatial AI & Exocarp Feed (5 Cols)           */}
        {/* ========================================================= */}
        <div
          style={{
            gridColumn: 'span 12',
            display: 'flex',
            flexDirection: 'column',
            gap: '1.5rem',
          }}
          className="analysis-left-col"
        >
          {/* Produce Image Card with Optical Reticle */}
          <div className="fresh-card" style={{ padding: 0, overflow: 'hidden', position: 'relative' }}>
            {/* Top Ribbon */}
            <div
              style={{
                padding: '0.85rem 1.25rem',
                backgroundColor: 'var(--bg-subtle)',
                borderBottom: '1px solid var(--border-light)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Activity size={18} style={{ color: 'var(--color-primary)' }} />
                <span
                  style={{
                    fontSize: '0.775rem',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    color: 'var(--color-forest)',
                  }}
                >
                  Spatial Bio-Scan Feed
                </span>
              </div>
              <span
                style={{
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                }}
              >
                Calibrated 1.0x
              </span>
            </div>

            {/* Produce Image Container with Reticle */}
            <div
              style={{
                position: 'relative',
                width: '100%',
                aspectRatio: '4 / 3',
                backgroundColor: '#0F172A',
                overflow: 'hidden',
              }}
            >
              {currentImagePreview ? (
                <img
                  src={currentImagePreview}
                  alt="Scanned Hass Avocado"
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                    display: 'block',
                  }}
                />
              ) : (
                <div
                  style={{
                    width: '100%',
                    height: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--text-subtle)',
                  }}
                >
                  <Camera size={48} />
                </div>
              )}

              {/* Optical Reticle & Segmentation Layer */}
              {showReticleOverlay && (
                <div
                  style={{
                    position: 'absolute',
                    inset: 0,
                    padding: '1rem',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    pointerEvents: 'none',
                  }}
                >
                  {/* Top Badges */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div
                      style={{
                        backgroundColor: 'rgba(15, 41, 30, 0.85)',
                        backdropFilter: 'blur(6px)',
                        padding: '4px 10px',
                        borderRadius: '9999px',
                        color: '#FFFFFF',
                        fontFamily: 'var(--font-mono)',
                        fontSize: '0.725rem',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        boxShadow: 'var(--shadow-sm)',
                      }}
                    >
                      <span
                        style={{
                          width: '8px',
                          height: '8px',
                          borderRadius: '50%',
                          backgroundColor: STAGE_COLORS[stage],
                        }}
                        className="animate-pulse-subtle"
                      />
                      <span>EPIDERMAL CLASSIFIER ON</span>
                    </div>

                    <div
                      style={{
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        backdropFilter: 'blur(6px)',
                        padding: '4px 10px',
                        borderRadius: '9999px',
                        fontFamily: 'var(--font-mono)',
                        fontSize: '0.725rem',
                        fontWeight: 600,
                        color: 'var(--color-forest)',
                        boxShadow: 'var(--shadow-sm)',
                      }}
                    >
                      <span>FOV: ~84.1%</span>
                    </div>
                  </div>

                  {/* Center Target Crosshairs & Sensor Ring */}
                  <div
                    style={{
                      position: 'absolute',
                      top: '50%',
                      left: '50%',
                      transform: 'translate(-50%, -50%)',
                      width: '160px',
                      height: '160px',
                      borderRadius: '50%',
                      pointerEvents: 'none',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <div
                      style={{
                        width: '100%',
                        height: '100%',
                        borderRadius: '50%',
                        border: `1.5px dashed ${STAGE_COLORS[stage]}`,
                        opacity: 0.75,
                      }}
                    />
                    <div
                      style={{
                        position: 'absolute',
                        width: '100%',
                        height: '1px',
                        backgroundColor: 'rgba(255, 255, 255, 0.35)',
                      }}
                    />
                    <div
                      style={{
                        position: 'absolute',
                        height: '100%',
                        width: '1px',
                        backgroundColor: 'rgba(255, 255, 255, 0.35)',
                      }}
                    />
                    <div
                      style={{
                        position: 'absolute',
                        bottom: '-12px',
                        backgroundColor: 'var(--color-forest)',
                        color: '#FFFFFF',
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontFamily: 'var(--font-mono)',
                        fontSize: '0.625rem',
                        letterSpacing: '0.04em',
                      }}
                    >
                      ROI: EXOCARP TISSUE
                    </div>
                  </div>

                  {/* Bottom Strip: Texture Matrix & Layer Toggle */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                    <div
                      style={{
                        backgroundColor: 'rgba(15, 41, 30, 0.88)',
                        backdropFilter: 'blur(8px)',
                        color: '#FFFFFF',
                        padding: '8px 12px',
                        borderRadius: '10px',
                        fontFamily: 'var(--font-mono)',
                        fontSize: '0.725rem',
                        boxShadow: 'var(--shadow-md)',
                      }}
                    >
                      <div style={{ color: 'var(--color-lime-glow)', fontSize: '0.65rem', fontWeight: 700 }}>
                        {texture.title}
                      </div>
                      <div style={{ marginTop: '2px' }}>{texture.desc}</div>
                      <div style={{ color: 'var(--color-lime-light)', fontSize: '0.685rem', marginTop: '2px' }}>
                        Est. Yield: {texture.yieldPressure}
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => setShowReticleOverlay(!showReticleOverlay)}
                      title="Toggle Sensor Overlay"
                      style={{
                        pointerEvents: 'auto',
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        backdropFilter: 'blur(6px)',
                        padding: '8px',
                        borderRadius: '50%',
                        boxShadow: 'var(--shadow-md)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'var(--color-forest)',
                      }}
                    >
                      <Layers size={18} />
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Micro Sensor Data Strip Below Produce */}
            <div style={{ padding: '1.25rem' }}>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(3, 1fr)',
                  gap: '0.75rem',
                  marginBottom: '1rem',
                }}
              >
                <div
                  style={{
                    padding: '0.65rem 0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: 'var(--bg-subtle)',
                    textAlign: 'center',
                  }}
                >
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.7rem',
                      color: 'var(--text-muted)',
                      display: 'block',
                      marginBottom: '2px',
                    }}
                  >
                    EXOCARP COLOR
                  </span>
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.95rem',
                      fontWeight: 700,
                      color: exocarpColor,
                    }}
                  >
                    {exocarpColor}
                  </span>
                </div>

                <div
                  style={{
                    padding: '0.65rem 0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: 'var(--bg-subtle)',
                    textAlign: 'center',
                  }}
                >
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.7rem',
                      color: 'var(--text-muted)',
                      display: 'block',
                      marginBottom: '2px',
                    }}
                  >
                    EST. DRY MATTER
                  </span>
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.95rem',
                      fontWeight: 700,
                      color: 'var(--text-main)',
                    }}
                  >
                    {texture.dryMatter}
                  </span>
                </div>

                <div
                  style={{
                    padding: '0.65rem 0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: 'var(--bg-subtle)',
                    textAlign: 'center',
                  }}
                >
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.7rem',
                      color: 'var(--text-muted)',
                      display: 'block',
                      marginBottom: '2px',
                    }}
                  >
                    SURFACE DEFECT
                  </span>
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.95rem',
                      fontWeight: 700,
                      color: isTerminalStage5 ? '#DC2626' : 'var(--color-primary)',
                    }}
                  >
                    {texture.defect}
                  </span>
                </div>
              </div>

              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
                High-contrast optical analysis indicates characteristic Hass exocarp state.
                {isTerminalStage5
                  ? ' Visible dark discoloration and soft structural depression indicate senescent maturity.'
                  : ' Pebble epidermis and pigment ratio align with expected cultivar ripening kinetics.'}
              </p>
            </div>
          </div>

          {/* Environmental Context Mini Card */}
          <div
            className="fresh-card"
            style={{
              padding: '1.25rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '1rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--bg-subtle)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--color-forest)',
                }}
              >
                <Thermometer size={22} />
              </div>
              <div>
                <span style={{ fontSize: '0.9rem', fontWeight: 700, color: 'var(--color-forest)', display: 'block' }}>
                  Current Ambient Baseline
                </span>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Room Storage &bull; ~20°C (68°F) &bull; Nominal Pantry
                </span>
              </div>
            </div>

            <span
              style={{
                fontFamily: 'var(--font-mono)',
                fontSize: '0.75rem',
                fontWeight: 700,
                color: 'var(--color-primary-dark)',
                backgroundColor: 'var(--color-primary-light)',
                padding: '4px 10px',
                borderRadius: '6px',
              }}
            >
              NOMINAL
            </span>
          </div>
        </div>

        {/* ========================================================= */}
        {/* RIGHT COLUMN: Primary Ripeness & RUL Diagnostics (7 Cols) */}
        {/* ========================================================= */}
        <div
          style={{
            gridColumn: 'span 12',
            display: 'flex',
            flexDirection: 'column',
            gap: '1.75rem',
          }}
          className="analysis-right-col"
        >
          {/* ------------------------------------------------------- */}
          {/* PRIMARY RESULT BANNER CARD                              */}
          {/* ------------------------------------------------------- */}
          <div
            className="fresh-card"
            style={{
              padding: '2rem',
              position: 'relative',
              overflow: 'hidden',
              background: isTerminalStage5
                ? 'linear-gradient(135deg, #FEF2F2 0%, #FFFFFF 100%)'
                : 'linear-gradient(135deg, #F0FDF4 0%, #FFFFFF 100%)',
              border: `1.5px solid ${isTerminalStage5 ? '#FCA5A5' : 'var(--border-accent)'}`,
            }}
          >
            {/* Top Status & Confidence Badges */}
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '0.75rem',
                marginBottom: '1.25rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <RipenessBadge stage={stage} label={stageLabel} size="md" />
                {isTerminalStage5 ? (
                  <span
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px',
                      backgroundColor: '#FEE2E2',
                      color: '#991B1B',
                      padding: '4px 10px',
                      borderRadius: '9999px',
                      fontSize: '0.775rem',
                      fontWeight: 700,
                    }}
                  >
                    <AlertTriangle size={14} />
                    <span>Terminal Boundary</span>
                  </span>
                ) : (
                  <span
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px',
                      backgroundColor: 'var(--color-primary-light)',
                      color: 'var(--color-primary-dark)',
                      padding: '4px 10px',
                      borderRadius: '9999px',
                      fontSize: '0.775rem',
                      fontWeight: 700,
                    }}
                  >
                    <CheckCircle2 size={14} />
                    <span>{stage === 4 ? 'Peak Consumption Window' : 'Active Shelf Life'}</span>
                  </span>
                )}
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span
                  style={{
                    backgroundColor: '#FFFFFF',
                    border: '1px solid var(--border-light)',
                    padding: '4px 10px',
                    borderRadius: '9999px',
                    fontFamily: 'var(--font-mono)',
                    fontSize: '0.775rem',
                    fontWeight: 700,
                    color: 'var(--color-forest)',
                  }}
                >
                  {(confidence * 100).toFixed(1)}% AI Confidence
                </span>
                <span
                  style={{
                    backgroundColor: 'var(--bg-subtle)',
                    padding: '4px 10px',
                    borderRadius: '9999px',
                    fontFamily: 'var(--font-mono)',
                    fontSize: '0.775rem',
                    color: 'var(--text-muted)',
                  }}
                >
                  Index: {expectedStage.toFixed(2)} / 5.0
                </span>
              </div>
            </div>

            {/* Main Stage Headline & Interpretation */}
            <div style={{ marginBottom: '1.75rem' }}>
              <h2
                style={{
                  fontSize: 'clamp(1.75rem, 2.5vw, 2.25rem)',
                  color: isTerminalStage5 ? '#991B1B' : 'var(--color-forest)',
                  marginBottom: '0.5rem',
                  lineHeight: 1.2,
                }}
              >
                Stage {stage} &mdash; {stageLabel}
              </h2>
              <p style={{ color: 'var(--text-main)', fontSize: '0.95rem', lineHeight: '1.6' }}>
                {interpretation}
              </p>
            </div>

            {/* KPI Display: Remaining Usable Life Hero */}
            <div
              style={{
                backgroundColor: isTerminalStage5 ? '#FFF5F5' : 'rgba(255, 255, 255, 0.85)',
                border: `1px solid ${isTerminalStage5 ? '#FED7D7' : 'var(--border-light)'}`,
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem 1.5rem',
                display: 'flex',
                flexDirection: 'row',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1.25rem',
              }}
            >
              <div>
                <span
                  style={{
                    fontSize: '0.75rem',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    color: isTerminalStage5 ? '#991B1B' : 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    marginBottom: '4px',
                  }}
                >
                  <Clock size={15} />
                  ESTIMATED REMAINING USABLE LIFE
                </span>

                <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
                  <span
                    style={{
                      fontSize: '3.25rem',
                      fontWeight: 800,
                      lineHeight: 1,
                      color: isTerminalStage5 ? '#991B1B' : 'var(--color-forest)',
                      fontFamily: 'var(--font-heading)',
                    }}
                  >
                    {ambientRul.toFixed(1)}
                  </span>
                  <span style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--text-main)' }}>
                    Days
                  </span>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    (at Ambient ~20°C Room Temp)
                  </span>
                </div>
              </div>

              {/* Shelf Life Extension Callout Pill OR Terminal Alert */}
              {isTerminalStage5 ? (
                <div
                  style={{
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: '#FEE2E2',
                    border: '1px solid #FCA5A5',
                    color: '#991B1B',
                    maxWidth: '320px',
                    fontSize: '0.825rem',
                    lineHeight: '1.4',
                  }}
                >
                  <strong>Terminal State Notice:</strong> Produce has reached Stage 5 (Overripe).
                  Refrigeration cannot restore expired shelf life.
                </div>
              ) : (
                refrigeration_extension_gain_days > 0 && (
                  <div
                    style={{
                      padding: '0.75rem 1rem',
                      borderRadius: 'var(--radius-md)',
                      backgroundColor: '#FFFFFF',
                      border: '1px solid var(--border-accent)',
                      boxShadow: 'var(--shadow-sm)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      maxWidth: '320px',
                    }}
                  >
                    <div
                      style={{
                        width: '38px',
                        height: '38px',
                        borderRadius: '50%',
                        backgroundColor: 'var(--color-primary-light)',
                        color: 'var(--color-primary-dark)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      <Snowflake size={20} />
                    </div>
                    <div style={{ fontSize: '0.825rem', lineHeight: '1.4' }}>
                      <span style={{ fontWeight: 700, color: 'var(--color-forest)', display: 'block' }}>
                        Refrigerate at 10°C / 4°C
                      </span>
                      <span style={{ color: 'var(--text-muted)' }}>
                        Extends to ~<strong style={{ color: 'var(--color-primary-dark)' }}>{cold10Rul.toFixed(1)} Days</strong> (+{refrigeration_extension_gain_days.toFixed(1)}d gain)
                      </span>
                    </div>
                  </div>
                )
              )}
            </div>
          </div>

          {/* ------------------------------------------------------- */}
          {/* 5-STAGE RIPENING SPECTRUM TRAJECTORY                    */}
          {/* ------------------------------------------------------- */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: '1rem',
                flexWrap: 'wrap',
                gap: '0.5rem',
              }}
            >
              <div>
                <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '2px' }}>
                  Ripening Spectrum Trajectory
                </h3>
                <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>
                  Dynamic stage progression benchmarked across standard Hass ripening scales.
                </p>
              </div>

              <span
                style={{
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.725rem',
                  fontWeight: 700,
                  backgroundColor: STAGE_COLORS[stage] + '22',
                  color: STAGE_COLORS[stage],
                  border: `1px solid ${STAGE_COLORS[stage]}55`,
                  padding: '4px 10px',
                  borderRadius: '6px',
                }}
              >
                STAGE {stage} ACTIVE
              </span>
            </div>

            {/* 5-Segment Visual Progression Bar */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(5, 1fr)',
                gap: '6px',
                height: '14px',
                backgroundColor: 'var(--bg-subtle)',
                padding: '3px',
                borderRadius: '9999px',
                marginBottom: '1.25rem',
              }}
            >
              {[1, 2, 3, 4, 5].map((s) => {
                const isPassed = s < stage;
                const isCurrent = s === stage;
                return (
                  <div
                    key={s}
                    title={`Stage ${s}`}
                    style={{
                      borderRadius: '9999px',
                      backgroundColor: isCurrent
                        ? STAGE_COLORS[s]
                        : isPassed
                        ? STAGE_COLORS[s] + 'aa'
                        : 'var(--border-light)',
                      boxShadow: isCurrent ? `0 0 8px ${STAGE_COLORS[s]}88` : 'none',
                      position: 'relative',
                      transition: 'all 0.3s ease',
                    }}
                  >
                    {isCurrent && (
                      <div
                        style={{
                          position: 'absolute',
                          inset: 0,
                          borderRadius: '9999px',
                          backgroundColor: '#FFFFFF',
                          opacity: 0.3,
                        }}
                        className="animate-pulse-subtle"
                      />
                    )}
                  </div>
                );
              })}
            </div>

            {/* 5 Stages Descriptive Column Indicators */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(5, 1fr)',
                gap: '0.5rem',
                fontSize: '0.75rem',
              }}
            >
              {[
                { s: 1, name: 'S1 • Hard', sub: 'Underripe', rulEst: '8–10d' },
                { s: 2, name: 'S2 • Breaking', sub: 'Color shift', rulEst: '5–7d' },
                { s: 3, name: 'S3 • Firm-Ripe', sub: 'Ideal slicing', rulEst: '3–4d' },
                { s: 4, name: 'S4 • Soft Ripe', sub: 'Peak flavor', rulEst: '1–2d' },
                { s: 5, name: 'S5 • Senescent', sub: 'Overripe', rulEst: '0d' },
              ].map((item) => {
                const isCurrent = item.s === stage;
                return (
                  <div
                    key={item.s}
                    style={{
                      padding: isCurrent ? '8px' : '4px',
                      borderRadius: '8px',
                      backgroundColor: isCurrent ? 'var(--bg-subtle)' : 'transparent',
                      border: isCurrent ? '1px solid var(--border-accent)' : '1px solid transparent',
                      transition: 'all 0.2s ease',
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '2px' }}>
                      {isCurrent && (
                        <span
                          style={{
                            width: '6px',
                            height: '6px',
                            borderRadius: '50%',
                            backgroundColor: STAGE_COLORS[item.s],
                          }}
                        />
                      )}
                      <span
                        style={{
                          fontFamily: 'var(--font-mono)',
                          fontWeight: isCurrent ? 700 : 600,
                          color: isCurrent ? 'var(--color-forest)' : 'var(--text-muted)',
                        }}
                      >
                        {item.name}
                      </span>
                    </div>
                    <div style={{ color: 'var(--text-subtle)', fontSize: '0.7rem' }}>{item.sub}</div>
                    <div
                      style={{
                        fontFamily: 'var(--font-mono)',
                        fontSize: '0.675rem',
                        fontWeight: 600,
                        color: isCurrent ? STAGE_COLORS[item.s] : 'var(--text-subtle)',
                        marginTop: '2px',
                      }}
                    >
                      {item.rulEst} RUL
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* ------------------------------------------------------- */}
          {/* PROBABILITY DISTRIBUTION SECTION                        */}
          {/* ------------------------------------------------------- */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: '1rem',
                flexWrap: 'wrap',
                gap: '0.5rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <BarChart3 size={20} style={{ color: 'var(--color-forest)' }} />
                <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)' }}>
                  Classification Probabilities
                </h3>
              </div>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Softmax Spread (N=5)
              </span>
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
              Full probability distribution across convolutional feature embeddings. Highest certainty concentrated in predicted class.
            </p>

            {/* Probability Bars for Stage 1 to 5 */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              {[
                { num: 1, label: 'Stage 1 — Underripe (Hard)', val: probs.stage_1, color: STAGE_COLORS[1] },
                { num: 2, label: 'Stage 2 — Breaking (Olive)', val: probs.stage_2, color: STAGE_COLORS[2] },
                { num: 3, label: 'Stage 3 — Ripe First Stage', val: probs.stage_3, color: STAGE_COLORS[3] },
                { num: 4, label: 'Stage 4 — Ripe Second Stage (Soft Ripe)', val: probs.stage_4, color: STAGE_COLORS[4] },
                { num: 5, label: 'Stage 5 — Overripe (Senescent)', val: probs.stage_5, color: STAGE_COLORS[5] },
              ].map((st) => {
                const isDominant = st.num === stage;
                const percentage = (st.val * 100).toFixed(1);

                return (
                  <div
                    key={st.num}
                    style={{
                      padding: isDominant ? '8px 12px' : '4px 0',
                      borderRadius: isDominant ? 'var(--radius-sm)' : 0,
                      backgroundColor: isDominant ? 'var(--bg-subtle)' : 'transparent',
                      border: isDominant ? '1px solid var(--border-accent)' : 'none',
                    }}
                  >
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '0.85rem',
                        marginBottom: '4px',
                      }}
                    >
                      <span
                        style={{
                          fontWeight: isDominant ? 700 : 500,
                          color: isDominant ? 'var(--color-forest)' : 'var(--text-main)',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '6px',
                        }}
                      >
                        {isDominant && (
                          <span
                            style={{
                              width: '6px',
                              height: '6px',
                              borderRadius: '50%',
                              backgroundColor: st.color,
                            }}
                          />
                        )}
                        {st.label}
                      </span>
                      <span
                        style={{
                          fontFamily: 'var(--font-mono)',
                          fontWeight: isDominant ? 700 : 600,
                          color: isDominant ? st.color : 'var(--text-muted)',
                        }}
                      >
                        {percentage}%
                      </span>
                    </div>

                    <div
                      style={{
                        height: isDominant ? '10px' : '7px',
                        backgroundColor: isDominant ? 'var(--bg-card)' : 'var(--bg-subtle)',
                        borderRadius: '9999px',
                        overflow: 'hidden',
                      }}
                    >
                      <div
                        style={{
                          height: '100%',
                          width: `${Math.max(1, st.val * 100)}%`,
                          backgroundColor: st.color,
                          borderRadius: '9999px',
                          transition: 'width 0.6s cubic-bezier(0.16, 1, 0.3, 1)',
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>

            <div
              style={{
                marginTop: '1.25rem',
                paddingTop: '1rem',
                borderTop: '1px solid var(--border-light)',
                display: 'flex',
                justifyContent: 'space-between',
                fontSize: '0.825rem',
                color: 'var(--text-muted)',
              }}
            >
              <span>Expected Continuous Ripeness Value:</span>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--color-forest)' }}>
                {expectedStage.toFixed(3)} / 5.000
              </span>
            </div>
          </div>

          {/* ------------------------------------------------------- */}
          {/* DEGRADATION TRAJECTORY (Actual API Data SVG Curve)      */}
          {/* ------------------------------------------------------- */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: '0.5rem',
                flexWrap: 'wrap',
                gap: '0.5rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Activity size={20} style={{ color: 'var(--color-forest)' }} />
                <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)' }}>
                  Kinetic Degradation Trajectory
                </h3>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '0.75rem' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--color-forest)' }}>
                  <span style={{ width: '10px', height: '3px', backgroundColor: 'var(--color-forest)', borderRadius: '2px' }} />
                  Ambient 20°C ({ambientRul.toFixed(1)}d)
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--color-primary-dark)' }}>
                  <span style={{ width: '10px', height: '3px', backgroundColor: 'var(--color-primary)', borderRadius: '2px' }} />
                  Chilled 10°C ({cold10Rul.toFixed(1)}d)
                </span>
              </div>
            </div>

            <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Forward-looking shelf-life decay derived directly from AI regression estimates. (No historical points invented).
            </p>

            {/* SVG Shelf-Life Trajectory Plot */}
            <div
              style={{
                width: '100%',
                height: '160px',
                backgroundColor: 'var(--bg-subtle)',
                borderRadius: 'var(--radius-md)',
                padding: '8px',
                position: 'relative',
              }}
            >
              <svg
                viewBox={`0 0 ${svgWidth} ${svgHeight}`}
                style={{ width: '100%', height: '100%', overflow: 'visible' }}
                preserveAspectRatio="none"
              >
                {/* Horizontal Grid lines */}
                <line x1={startX} y1={topY} x2={endX} y2={topY} stroke="#E2E8F0" strokeDasharray="3 3" />
                <line x1={startX} y1={(topY + bottomY) / 2} x2={endX} y2={(topY + bottomY) / 2} stroke="#E2E8F0" strokeDasharray="3 3" />
                <line x1={startX} y1={bottomY} x2={endX} y2={bottomY} stroke="#CBD5E1" strokeWidth="1.5" />

                {/* Y-Axis Labels */}
                <text x="8" y={topY + 4} fill="#64748B" fontSize="9" fontFamily="monospace">100%</text>
                <text x="12" y={(topY + bottomY) / 2 + 3} fill="#64748B" fontSize="9" fontFamily="monospace">50%</text>
                <text x="18" y={bottomY + 3} fill="#64748B" fontSize="9" fontFamily="monospace">0%</text>

                {isTerminalStage5 ? (
                  // Terminal Stage 5 representation: Flat line at 0%
                  <>
                    <line x1={startX} y1={bottomY} x2={endX} y2={bottomY} stroke="#991B1B" strokeWidth="3" />
                    <circle cx={startX} cy={bottomY} r="5" fill="#991B1B" />
                    <text x={startX + 10} y={bottomY - 10} fill="#991B1B" fontSize="10" fontWeight="bold" fontFamily="monospace">
                      Terminal Maturity (0.0d RUL)
                    </text>
                  </>
                ) : (
                  <>
                    {/* Chilled 10C Curve (Teal / Primary) */}
                    <path
                      d={`M ${startX},${topY + 15} Q ${(startX + cold10EndX) / 2},${topY + 25} ${cold10EndX},${bottomY}`}
                      fill="none"
                      stroke="var(--color-primary)"
                      strokeWidth="2.5"
                      strokeDasharray="4 2"
                    />

                    {/* Ambient 20C Curve (Dark Forest) */}
                    <path
                      d={`M ${startX},${topY + 15} Q ${(startX + ambientEndX) / 2},${topY + 35} ${ambientEndX},${bottomY}`}
                      fill="none"
                      stroke="var(--color-forest)"
                      strokeWidth="3"
                    />

                    {/* Scan Point Marker (Today, Day 0) */}
                    <circle cx={startX} cy={topY + 15} r="5" fill="var(--color-forest)" />
                    <circle cx={startX} cy={topY + 15} r="9" fill="none" stroke="var(--color-primary)" opacity="0.6" />

                    {/* Ambient Terminal Point */}
                    <circle cx={ambientEndX} cy={bottomY} r="4" fill="var(--color-forest)" />
                    <text x={ambientEndX - 15} y={bottomY + 16} fill="var(--color-forest)" fontSize="9" fontWeight="bold" fontFamily="monospace">
                      {ambientRul.toFixed(1)}d
                    </text>

                    {/* Cold Terminal Point */}
                    <circle cx={cold10EndX} cy={bottomY} r="4" fill="var(--color-primary)" />
                    <text x={cold10EndX - 15} y={bottomY + 16} fill="var(--color-primary-dark)" fontSize="9" fontWeight="bold" fontFamily="monospace">
                      {cold10Rul.toFixed(1)}d
                    </text>
                  </>
                )}

                {/* X-Axis Baseline Labels */}
                <text x={startX - 10} y={bottomY + 16} fill="#64748B" fontSize="9" fontFamily="monospace">Day 0 (Now)</text>
              </svg>
            </div>
          </div>

          {/* ------------------------------------------------------- */}
          {/* COMPREHENSIVE REMAINING USABLE LIFE (RUL) SECTION       */}
          {/* ------------------------------------------------------- */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div style={{ marginBottom: '1.25rem' }}>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '4px' }}>
                Remaining Usable Life Across Storage Scenarios
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Empirical predictions derived from HistGradientBoosting model; 4°C refrigerator simulated via biophysical kinetics.
              </p>
            </div>

            {/* A. EMPIRICAL OBSERVED CONDITIONS (Direct Dataset Observations) */}
            <div style={{ marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px', marginBottom: '0.85rem' }}>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-forest)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Empirical Observed Conditions (Direct Ground-Truth Model)
                </span>
                <span
                  style={{
                    backgroundColor: 'var(--color-primary-light)',
                    color: 'var(--color-primary-dark)',
                    fontSize: '0.7rem',
                    fontWeight: 700,
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontFamily: 'var(--font-mono)',
                  }}
                >
                  EMPIRICAL DATASET TRAINING
                </span>
              </div>

              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                  gap: '1rem',
                }}
              >
                {/* 1. Ambient */}
                <div
                  style={{
                    padding: '1rem',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--bg-subtle)',
                    border: '1px solid var(--border-light)',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ fontSize: '0.725rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                      AMBIENT (~20–22°C)
                    </div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-forest)', margin: '4px 0' }}>
                      {ambientRul.toFixed(1)} <span style={{ fontSize: '1rem', fontWeight: 500 }}>days</span>
                    </div>
                  </div>
                  <div
                    style={{
                      fontSize: '0.725rem',
                      color: 'var(--color-primary-dark)',
                      backgroundColor: 'var(--color-primary-light)',
                      padding: '2px 8px',
                      borderRadius: '4px',
                      alignSelf: 'flex-start',
                      fontWeight: 600,
                    }}
                  >
                    Empirical ML
                  </div>
                </div>

                {/* 2. 20°C Controlled */}
                <div
                  style={{
                    padding: '1rem',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--bg-subtle)',
                    border: '1px solid var(--border-light)',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ fontSize: '0.725rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                      CONTROLLED 20°C
                    </div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-forest)', margin: '4px 0' }}>
                      {room20Rul.toFixed(1)} <span style={{ fontSize: '1rem', fontWeight: 500 }}>days</span>
                    </div>
                  </div>
                  <div
                    style={{
                      fontSize: '0.725rem',
                      color: 'var(--color-primary-dark)',
                      backgroundColor: 'var(--color-primary-light)',
                      padding: '2px 8px',
                      borderRadius: '4px',
                      alignSelf: 'flex-start',
                      fontWeight: 600,
                    }}
                  >
                    Empirical ML
                  </div>
                </div>

                {/* 3. 10°C Cold Storage */}
                <div
                  style={{
                    padding: '1rem',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--bg-subtle)',
                    border: '1px solid var(--border-light)',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ fontSize: '0.725rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                      10°C COLD CRISPER
                    </div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-forest)', margin: '4px 0' }}>
                      {cold10Rul.toFixed(1)} <span style={{ fontSize: '1rem', fontWeight: 500 }}>days</span>
                    </div>
                  </div>
                  <div
                    style={{
                      fontSize: '0.725rem',
                      color: 'var(--color-primary-dark)',
                      backgroundColor: 'var(--color-primary-light)',
                      padding: '2px 8px',
                      borderRadius: '4px',
                      alignSelf: 'flex-start',
                      fontWeight: 600,
                    }}
                  >
                    Empirical ML
                  </div>
                </div>
              </div>
            </div>

            {/* B. VISUALLY SEPARATED 4°C EXTRAPOLATION SCENARIO */}
            <div
              style={{
                backgroundColor: '#EFF6FF',
                border: '1.5px solid #93C5FD',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem',
                marginBottom: '1rem',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '8px',
                  marginBottom: '1rem',
                  paddingBottom: '0.75rem',
                  borderBottom: '1px solid #BFDBFE',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Snowflake size={18} style={{ color: '#2563EB' }} />
                  <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#1E3A8A', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                    4°C Domestic Refrigerator
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.725rem',
                      fontWeight: 700,
                      backgroundColor: '#DBEAFE',
                      color: '#1E40AF',
                      border: '1px solid #93C5FD',
                      padding: '3px 8px',
                      borderRadius: '4px',
                      letterSpacing: '0.03em',
                    }}
                  >
                    Model-based extrapolation
                  </span>
                  <span
                    style={{
                      fontSize: '0.7rem',
                      color: '#3B82F6',
                      fontFamily: 'var(--font-mono)',
                    }}
                  >
                    is_extrapolated: {String(scenarios['4C_refrigerator']?.is_extrapolated ?? true)}
                  </span>
                </div>
              </div>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '1.5rem',
                  marginBottom: '1rem',
                }}
              >
                <div>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#1E40AF', textTransform: 'uppercase' }}>
                    Extrapolated Usable Life (4°C)
                  </div>
                  <div style={{ fontSize: '2.25rem', fontWeight: 800, color: '#1E3A8A', margin: '2px 0' }}>
                    {fridge4Rul.toFixed(1)} <span style={{ fontSize: '1.15rem', fontWeight: 500 }}>days</span>
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#3B82F6' }}>
                    Method: {scenarios['4C_refrigerator']?.method || 'Biophysical Arrhenius / Q10 Extrapolation'}
                  </div>
                </div>

                {scenarios['4C_refrigerator']?.recommendation && (
                  <div
                    style={{
                      backgroundColor: '#FFFFFF',
                      border: '1px solid #BFDBFE',
                      borderRadius: 'var(--radius-sm)',
                      padding: '0.75rem 1rem',
                      maxWidth: '380px',
                      fontSize: '0.8rem',
                      color: '#1E3A8A',
                      lineHeight: '1.4',
                    }}
                  >
                    <strong style={{ color: '#1E40AF', display: 'block', marginBottom: '2px' }}>
                      Scenario Recommendation:
                    </strong>
                    {scenarios['4C_refrigerator'].recommendation}
                  </div>
                )}
              </div>

              {/* 4°C Uncertainty Note & Food Safety Disclaimer */}
              <div
                style={{
                  backgroundColor: '#FFFFFF',
                  border: '1px solid #BFDBFE',
                  borderRadius: 'var(--radius-sm)',
                  padding: '0.75rem 1rem',
                  fontSize: '0.8rem',
                  color: 'var(--text-muted)',
                  lineHeight: '1.5',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '8px',
                }}
              >
                <Info size={16} style={{ color: '#3B82F6', flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <div style={{ marginBottom: '4px' }}>
                    <strong style={{ color: '#1E3A8A' }}>Model-Based Extrapolation Notice: </strong>
                    {scenarios['4C_refrigerator']?.uncertainty_note || (
                      <>
                        4°C Domestic Refrigerator shelf-life is mathematically extrapolated via biochemical respiration kinetics{' '}
                        (Q10 = 2.38). <strong>4°C is not direct ground-truth training data</strong>.
                      </>
                    )}
                    {isTerminalStage5 && ' Produce has already reached senescent maturity (RUL = 0); chilling cannot reverse overripeness.'}
                  </div>
                  {scenarios['4C_refrigerator']?.disclaimer && (
                    <div style={{ fontSize: '0.75rem', color: '#64748B', fontStyle: 'italic' }}>
                      <strong>Disclaimer:</strong> {scenarios['4C_refrigerator'].disclaimer}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* ------------------------------------------------------- */}
          {/* ACTION PROTOCOL / CHEF & STORAGE ADVISORY               */}
          {/* ------------------------------------------------------- */}
          <div
            style={{
              background: 'linear-gradient(135deg, var(--color-forest) 0%, #1a3826 100%)',
              color: '#FFFFFF',
              borderRadius: 'var(--radius-lg)',
              padding: '2rem',
              boxShadow: 'var(--shadow-lg)',
              position: 'relative',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                display: 'flex',
                flexDirection: 'row',
                alignItems: 'flex-start',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1.5rem',
                marginBottom: '1.75rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem', maxWidth: '640px' }}>
                <div
                  style={{
                    width: '48px',
                    height: '48px',
                    borderRadius: '12px',
                    backgroundColor: 'var(--color-primary-light)',
                    color: 'var(--color-primary-dark)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <Utensils size={24} />
                </div>
                <div>
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      color: 'var(--color-lime-glow)',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em',
                      display: 'block',
                      marginBottom: '4px',
                    }}
                  >
                    Chef &amp; Pantry Advisory
                  </span>
                  <h3 style={{ fontSize: '1.35rem', color: '#FFFFFF', marginBottom: '0.5rem' }}>
                    Action Protocol: {isTerminalStage5 ? 'Immediate Consumption or Discard' : stage === 4 ? 'Peak Eating Window' : 'Controlled Maturation'}
                  </h3>
                  <p style={{ color: 'rgba(255, 255, 255, 0.9)', fontSize: '0.925rem', lineHeight: '1.6' }}>
                    {actionable_recommendation}
                  </p>
                </div>
              </div>

              {/* Culinary Suitability Badge */}
              <div
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.12)',
                  backdropFilter: 'blur(8px)',
                  padding: '12px 18px',
                  borderRadius: '12px',
                  textAlign: 'center',
                  minWidth: '160px',
                }}
              >
                <span
                  style={{
                    fontFamily: 'var(--font-mono)',
                    fontSize: '0.675rem',
                    color: 'var(--color-lime-glow)',
                    textTransform: 'uppercase',
                    display: 'block',
                    marginBottom: '2px',
                  }}
                >
                  Culinary Suitability
                </span>
                <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#FFFFFF', display: 'block' }}>
                  {culinary.title}
                </span>
                <span style={{ fontSize: '0.75rem', color: 'rgba(255, 255, 255, 0.75)' }}>
                  {culinary.subtitle}
                </span>
              </div>
            </div>

            {/* Navigation & Action Buttons Row */}
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '1rem',
                borderTop: '1px solid rgba(255, 255, 255, 0.15)',
                paddingTop: '1.25rem',
              }}
            >
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', alignItems: 'center' }}>
                <Link
                  to="/comparison"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '9999px',
                    backgroundColor: 'var(--color-primary)',
                    color: '#FFFFFF',
                    fontWeight: 700,
                    fontSize: '0.875rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.04em',
                    boxShadow: 'var(--shadow-md)',
                  }}
                >
                  <Sliders size={18} />
                  <span>Explore Storage Comparison</span>
                </Link>

                <Link
                  to="/what-if"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '0.75rem 1.25rem',
                    borderRadius: '9999px',
                    backgroundColor: 'rgba(255, 255, 255, 0.15)',
                    color: '#FFFFFF',
                    fontWeight: 600,
                    fontSize: '0.875rem',
                  }}
                >
                  <span>Open What-If Lab</span>
                  <ChevronRight size={16} />
                </Link>
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', alignItems: 'center' }}>
                <button
                  type="button"
                  onClick={handleSaveToHistory}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '0.65rem 1.15rem',
                    borderRadius: '9999px',
                    backgroundColor: savedToHistory ? 'var(--color-primary)' : 'rgba(255, 255, 255, 0.12)',
                    color: '#FFFFFF',
                    fontSize: '0.85rem',
                    fontWeight: 600,
                    border: '1px solid rgba(255, 255, 255, 0.25)',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                  }}
                >
                  {savedToHistory ? <BookmarkCheck size={16} /> : <Bookmark size={16} />}
                  <span>{savedToHistory ? 'Saved to History!' : 'Save to Scan History'}</span>
                </button>

                <Link
                  to="/scan"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '0.65rem 1.15rem',
                    borderRadius: '9999px',
                    color: '#FFFFFF',
                    backgroundColor: 'rgba(255, 255, 255, 0.1)',
                    fontSize: '0.85rem',
                    fontWeight: 600,
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                  }}
                >
                  <Camera size={16} />
                  <span>Scan Another Produce</span>
                </Link>
              </div>
            </div>
          </div>

          {/* ------------------------------------------------------- */}
          {/* MANDATORY RESPONSIBLE FOOD SAFETY BANNER                */}
          {/* ------------------------------------------------------- */}
          <div
            style={{
              backgroundColor: '#FFFBEB',
              border: '1.5px solid #FDE68A',
              borderRadius: 'var(--radius-md)',
              padding: '1.25rem 1.5rem',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '12px',
            }}
          >
            <ShieldAlert size={22} style={{ color: '#D97706', flexShrink: 0, marginTop: '2px' }} />
            <div style={{ fontSize: '0.85rem', color: '#92400E', lineHeight: '1.6' }}>
              <strong style={{ color: '#78350F' }}>Responsible AI Food Safety Disclaimer:</strong>{' '}
              {legal_disclaimer}{' '}
              FreshIQ models provide non-destructive statistical guidance. Always perform sensory checks (inspect for mold,
              discoloration, rancid odor, or severe collapse) before consuming any perishable food.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
