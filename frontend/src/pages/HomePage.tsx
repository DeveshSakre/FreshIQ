import React from 'react';
import { Link } from 'react-router-dom';
import {
  Camera,
  PlayCircle,
  Verified,
  TrendingUp,
  Timer,
  CheckCircle2,
  Hourglass,
  Layers,
  Thermometer,
  Utensils,
  Sprout,
  ArrowRight,
  ShieldCheck
} from 'lucide-react';

export const HomePage: React.FC = () => {
  return (
    <div style={{ width: '100%', overflowX: 'hidden' }}>
      {/* Subtle ambient lighting glows */}
      <div
        style={{
          position: 'absolute',
          top: '70px',
          right: '15%',
          width: '450px',
          height: '450px',
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(163, 230, 53, 0.12) 0%, rgba(22, 163, 74, 0) 70%)',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />
      <div
        style={{
          position: 'absolute',
          top: '400px',
          left: '5%',
          width: '400px',
          height: '400px',
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(20, 184, 166, 0.08) 0%, rgba(15, 41, 30, 0) 70%)',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />

      {/* 1. HERO SECTION (Split Grid) */}
      <section className="container" style={{ position: 'relative', zIndex: 1, paddingTop: '2.5rem', paddingBottom: '4.5rem' }}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '3.5rem',
            alignItems: 'center',
          }}
        >
          {/* Left Column: Scientific Pitch & CTAs */}
          <div style={{ maxWidth: '640px' }}>
            {/* Status Pill */}
            <div
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '8px',
                padding: '6px 14px',
                borderRadius: '9999px',
                backgroundColor: 'var(--bg-card)',
                border: '1px solid var(--border-light)',
                boxShadow: 'var(--shadow-sm)',
                marginBottom: '1.5rem',
              }}
            >
              <span style={{ position: 'relative', display: 'flex', height: '8px', width: '8px' }}>
                <span
                  style={{
                    position: 'absolute',
                    display: 'inline-flex',
                    height: '100%',
                    width: '100%',
                    borderRadius: '50%',
                    backgroundColor: 'var(--color-primary)',
                    opacity: 0.75,
                  }}
                  className="animate-pulse-subtle"
                />
                <span
                  style={{
                    position: 'relative',
                    display: 'inline-flex',
                    borderRadius: '50%',
                    height: '8px',
                    width: '8px',
                    backgroundColor: 'var(--color-primary)',
                  }}
                />
              </span>
              <span
                style={{
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  letterSpacing: '0.04em',
                  color: 'var(--color-forest)',
                  textTransform: 'uppercase',
                }}
              >
                AI-Powered Freshness Intelligence &bull; Hass Avocado v1.2
              </span>
            </div>

            {/* Headline */}
            <h1
              style={{
                fontSize: 'clamp(2.5rem, 5.5vw, 3.8rem)',
                fontWeight: 800,
                color: 'var(--color-forest)',
                letterSpacing: '-0.03em',
                lineHeight: 1.12,
                marginBottom: '1.25rem',
              }}
            >
              Know your produce.<br />
              <span style={{ color: 'var(--color-primary)' }}>Waste less.</span>
            </h1>

            {/* Supporting Explanation */}
            <p
              style={{
                fontSize: '1.125rem',
                color: 'var(--text-muted)',
                lineHeight: 1.65,
                marginBottom: '2rem',
                maxWidth: '540px',
              }}
            >
              Computer vision and biophysical shelf-life modeling quantify ripeness in seconds, calculate remaining
              usable life across storage conditions, and deliver exact culinary timing to eat, chill, or freeze.
            </p>

            {/* CTA Group */}
            <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '1rem', marginBottom: '1.75rem' }}>
              <Link
                to="/scan"
                className="btn-primary"
                style={{
                  padding: '0.9rem 1.85rem',
                  fontSize: '1rem',
                  borderRadius: '9999px',
                  backgroundColor: 'var(--color-forest)',
                  boxShadow: '0 4px 14px rgba(15, 41, 30, 0.25)',
                }}
              >
                <Camera size={18} />
                <span>Scan Your Produce</span>
              </Link>

              <a
                href="#workflow"
                className="btn-secondary"
                style={{
                  padding: '0.9rem 1.5rem',
                  fontSize: '1rem',
                  borderRadius: '9999px',
                }}
              >
                <PlayCircle size={18} style={{ color: 'var(--color-primary)' }} />
                <span>How It Works</span>
              </a>
            </div>

            {/* Live Trust Metadata */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              <Verified size={18} style={{ color: 'var(--color-primary)', flexShrink: 0 }} />
              <span>Calibrated on 14,710 fruit time-series across ambient (20°C) and commercial cold storage (10°C).</span>
            </div>
          </div>

          {/* Right Column: Visual Showcase & Floating Telemetry Overlay */}
          <div style={{ position: 'relative', display: 'flex', justifyContent: 'center' }}>
            {/* Main Specimen Image Container */}
            <div
              style={{
                position: 'relative',
                width: '100%',
                maxWidth: '440px',
                borderRadius: 'var(--radius-xl)',
                overflow: 'hidden',
                boxShadow: 'var(--shadow-xl)',
                backgroundColor: '#FFFFFF',
                border: '1px solid var(--border-light)',
              }}
            >
              <img
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuDjF1Vxmny1tot6unZwd76wWmkO_lP0Kf0wjY0nWm9CZzDA69HL3wpPrBtcSbqh_F9WVF1aOrFuJilz-Gpsqa0IHF6GhsIppPEXYxv44ZfGyO0ACC8kO_6N7d7x9e7_4vMKxNa54oeWj8IzFXeYp_S7IgIBTSm2Rw-o63cvvsQdrup1N2KUzesLrU_xaDnlwpS38q_VkLQoSlbYjXeroNzDQOU1g-_nIZFs_C1cfZD2dFs1-fcGSAVDug"
                alt="Freshly sliced Hass avocado showing ripe lime green flesh and textured skin"
                style={{ width: '100%', height: '420px', objectFit: 'cover', display: 'block' }}
              />
              <div
                style={{
                  position: 'absolute',
                  inset: 0,
                  background: 'linear-gradient(to top, rgba(15, 41, 30, 0.75) 0%, transparent 50%)',
                  pointerEvents: 'none',
                }}
              />

              {/* Optical Reticle Badge */}
              <div
                style={{
                  position: 'absolute',
                  top: '16px',
                  right: '16px',
                  padding: '6px 12px',
                  borderRadius: '9999px',
                  backgroundColor: 'rgba(255, 255, 255, 0.88)',
                  backdropFilter: 'blur(8px)',
                  color: 'var(--color-forest)',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.725rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  boxShadow: 'var(--shadow-sm)',
                }}
              >
                <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-primary)' }} className="animate-pulse-subtle" />
                <span>CALIBRATED : OPTICAL V4</span>
              </div>

              <div style={{ position: 'absolute', bottom: '16px', left: '16px', right: '16px', color: '#FFFFFF' }}>
                <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.725rem', color: 'var(--color-lime-glow)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Botanical Specimen
                </div>
                <div style={{ fontSize: '1.2rem', fontWeight: 700 }}>
                  Persea americana (Hass Cultivar)
                </div>
              </div>
            </div>

            {/* Overlapping Diagnostic Telemetry Card */}
            <div
              style={{
                position: 'absolute',
                bottom: '-28px',
                left: '-10px',
                maxWidth: '330px',
                width: '95%',
                padding: '1.25rem',
                borderRadius: 'var(--radius-lg)',
                backgroundColor: '#FFFFFF',
                boxShadow: '0 20px 30px -8px rgba(15, 41, 30, 0.18)',
                border: '1px solid var(--border-light)',
                backdropFilter: 'blur(10px)',
              }}
              className="telemetry-card"
            >
              {/* Header Tag & Confidence */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--color-primary)', fontWeight: 700 }}>
                  Classification
                </span>
                <span
                  style={{
                    backgroundColor: 'var(--color-primary-light)',
                    color: 'var(--color-primary-dark)',
                    padding: '2px 8px',
                    borderRadius: '9999px',
                    fontFamily: 'var(--font-mono)',
                    fontSize: '0.7rem',
                    fontWeight: 700,
                  }}
                >
                  94.8% CONFIDENCE
                </span>
              </div>

              <div style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--color-forest)', marginBottom: '2px' }}>
                Stage 3 — Ripe First Stage
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '8px' }}>
                Continuous Ripeness Index: <strong style={{ color: 'var(--color-forest)', fontFamily: 'var(--font-mono)' }}>3.12</strong> / 5.0
              </div>

              {/* 5-Stage Segmented Bar */}
              <div style={{ display: 'flex', gap: '4px', alignItems: 'center', marginBottom: '4px' }}>
                <div style={{ height: '7px', flex: 1, borderRadius: '9999px', backgroundColor: 'var(--color-forest)' }} />
                <div style={{ height: '7px', flex: 1, borderRadius: '9999px', backgroundColor: 'var(--color-forest)' }} />
                <div style={{ height: '7px', flex: 1, borderRadius: '9999px', backgroundColor: 'var(--color-primary)', position: 'relative' }}>
                  <span
                    style={{
                      position: 'absolute',
                      top: '-3px',
                      left: '50%',
                      transform: 'translateX(-50%)',
                      width: '13px',
                      height: '13px',
                      borderRadius: '50%',
                      backgroundColor: 'var(--color-primary)',
                      border: '2px solid #FFFFFF',
                      boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
                    }}
                  />
                </div>
                <div style={{ height: '7px', flex: 1, borderRadius: '9999px', backgroundColor: 'var(--bg-subtle)' }} />
                <div style={{ height: '7px', flex: 1, borderRadius: '9999px', backgroundColor: 'var(--bg-subtle)' }} />
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.675rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: '10px' }}>
                <span>Hard (1)</span>
                <span style={{ color: 'var(--color-primary-dark)', fontWeight: 700 }}>Eating Peak</span>
                <span>Past Peak (5)</span>
              </div>

              {/* Degradation Forecast Block */}
              <div style={{ backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-sm)', padding: '8px 10px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.725rem', fontFamily: 'var(--font-mono)' }}>
                  <span style={{ color: 'var(--text-muted)' }}>RUL AMBIENT (20°C)</span>
                  <span style={{ fontWeight: 700, color: 'var(--color-forest)' }}>3.5 DAYS</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.725rem', fontFamily: 'var(--font-mono)' }}>
                  <span style={{ color: 'var(--color-primary)' }}>RUL CHILLED (10°C)</span>
                  <span style={{ fontWeight: 700, color: 'var(--color-primary)' }}>+5.5 DAYS EXT.</span>
                </div>
              </div>

              {/* Directive */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '8px', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 600 }}>
                <Utensils size={14} style={{ color: 'var(--color-primary)' }} />
                <span>Optimal for slicing, toast, and salads today.</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. PERFORMANCE HIGHLIGHT STRIP (Validated Benchmarks) */}
      <section style={{ backgroundColor: '#FFFFFF', borderTop: '1px solid var(--border-light)', borderBottom: '1px solid var(--border-light)', padding: '3.5rem 0' }}>
        <div className="container">
          <div style={{ textAlign: 'center', maxWidth: '640px', margin: '0 auto 2.5rem' }}>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.775rem', fontWeight: 700, color: 'var(--color-primary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
              Empirical Benchmarks
            </span>
            <h2 style={{ fontSize: '1.85rem', color: 'var(--color-forest)', marginTop: '4px' }}>
              Rigorous Evaluation on Untouched Test Split
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              Evaluated on 73 strictly isolated test specimens with zero overlap across train, validation, and test.
            </p>
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
              gap: '1.75rem',
            }}
          >
            {/* Metric 1 */}
            <div className="fresh-card" style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
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
                <TrendingUp size={24} />
              </div>
              <div>
                <div style={{ fontFamily: 'var(--font-heading)', fontSize: '2.2rem', fontWeight: 800, color: 'var(--color-forest)', lineHeight: 1.1 }}>
                  66.15%
                </div>
                <div style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--color-primary-dark)', textTransform: 'uppercase', letterSpacing: '0.04em', margin: '4px 0' }}>
                  Exact Stage Accuracy
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>
                  5-class ordinal classification using MobileNetV3-Small without synthetic augmentation inflation.
                </p>
              </div>
            </div>

            {/* Metric 2 */}
            <div className="fresh-card" style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
              <div
                style={{
                  width: '48px',
                  height: '48px',
                  borderRadius: '12px',
                  backgroundColor: '#ECFCCB',
                  color: '#3F6212',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <Timer size={24} />
              </div>
              <div>
                <div style={{ fontFamily: 'var(--font-heading)', fontSize: '2.2rem', fontWeight: 800, color: 'var(--color-forest)', lineHeight: 1.1 }}>
                  1.75 Days
                </div>
                <div style={{ fontSize: '0.875rem', fontWeight: 700, color: '#3F6212', textTransform: 'uppercase', letterSpacing: '0.04em', margin: '4px 0' }}>
                  RUL Mean Absolute Error
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>
                  Test MAE (~41.9h) across 2,112 test images with R² = 0.8420 tracking degradation to terminal Stage 5.
                </p>
              </div>
            </div>

            {/* Metric 3 */}
            <div className="fresh-card" style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
              <div
                style={{
                  width: '48px',
                  height: '48px',
                  borderRadius: '12px',
                  backgroundColor: '#DBEAFE',
                  color: '#1E40AF',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <CheckCircle2 size={24} />
              </div>
              <div>
                <div style={{ fontFamily: 'var(--font-heading)', fontSize: '2.2rem', fontWeight: 800, color: 'var(--color-forest)', lineHeight: 1.1 }}>
                  98.13%
                </div>
                <div style={{ fontSize: '0.875rem', fontWeight: 700, color: '#1E40AF', textTransform: 'uppercase', letterSpacing: '0.04em', margin: '4px 0' }}>
                  Exact or Adjacent Accuracy
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>
                  98.13% of all predictions are within ±1 developmental stage margin on test cohorts (0.358 stage ordinal MAE).
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. "FROM SNAPSHOT TO WASTE-FREE PANTRY" WORKFLOW (5 Scientific Steps) */}
      <section id="workflow" className="container" style={{ paddingTop: '4.5rem', paddingBottom: '4.5rem' }}>
        <div style={{ maxWidth: '680px', marginBottom: '3rem' }}>
          <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.775rem', fontWeight: 700, color: 'var(--color-primary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            System Pipeline
          </div>
          <h2 style={{ fontSize: '2.2rem', color: 'var(--color-forest)', marginTop: '4px', marginBottom: '0.75rem' }}>
            From snapshot to waste-free pantry in 5 scientific steps.
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', lineHeight: 1.6 }}>
            Harnessing computer vision and post-harvest degradation laws to eliminate culinary guesswork.
          </p>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1.25rem',
          }}
        >
          {/* Step 1 */}
          <div className="fresh-card" style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <span style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-forest)' }}>
                01
              </span>
              <Camera size={20} style={{ color: 'var(--color-primary)' }} />
            </div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem', color: 'var(--color-forest)' }}>Capture Image</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5, flex: 1 }}>
              Snap produce under natural room lighting. Multi-spectral chromatic features map skin reflectance and micro-texture.
            </p>
            <div style={{ marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.7rem', color: 'var(--color-primary)', fontWeight: 600 }}>
              EXIF & LIGHT CALIBRATION
            </div>
          </div>

          {/* Step 2 */}
          <div className="fresh-card" style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <span style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-forest)' }}>
                02
              </span>
              <Layers size={20} style={{ color: 'var(--color-primary)' }} />
            </div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem', color: 'var(--color-forest)' }}>Ripeness Stage</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5, flex: 1 }}>
              The MobileNetV3 neural backbone classifies 5 discrete developmental stages and computes a continuous ripeness index.
            </p>
            <div style={{ marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.7rem', color: 'var(--color-primary)', fontWeight: 600 }}>
              STAGES 1 TO 5
            </div>
          </div>

          {/* Step 3 */}
          <div className="fresh-card" style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <span style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-forest)' }}>
                03
              </span>
              <Hourglass size={20} style={{ color: 'var(--color-primary)' }} />
            </div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem', color: 'var(--color-forest)' }}>RUL Modeling</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5, flex: 1 }}>
              Calculates Remaining Usable Life using kinetic regression to forecast days until terminal Stage 5 overripeness.
            </p>
            <div style={{ marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.7rem', color: 'var(--color-primary)', fontWeight: 600 }}>
              ±1.75d TEST MAE
            </div>
          </div>

          {/* Step 4 */}
          <div className="fresh-card" style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <span style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-forest)' }}>
                04
              </span>
              <Thermometer size={20} style={{ color: 'var(--color-primary)' }} />
            </div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem', color: 'var(--color-forest)' }}>Thermal Sim</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5, flex: 1 }}>
              Simulates ambient (20°C), commercial cold (10°C), and refrigerator (4°C) with Arrhenius respiration kinetics.
            </p>
            <div style={{ marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.7rem', color: 'var(--color-primary)', fontWeight: 600 }}>
              AMBIENT VS REFRIGERATED
            </div>
          </div>

          {/* Step 5 */}
          <div className="fresh-card" style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <span style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-forest)' }}>
                05
              </span>
              <Utensils size={20} style={{ color: 'var(--color-primary)' }} />
            </div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem', color: 'var(--color-forest)' }}>Action Directive</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5, flex: 1 }}>
              Actionable kitchen directive: consume immediately, refrigerate to hold optimal peak, or transition to baking/guacamole.
            </p>
            <div style={{ marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.7rem', color: 'var(--color-primary)', fontWeight: 600 }}>
              ZERO-WASTE EXECUTION
            </div>
          </div>
        </div>
      </section>

      {/* 4. SUPPORTED PRODUCE SECTION (Honest Scope) */}
      <section className="container" style={{ paddingBottom: '4.5rem' }}>
        <div
          style={{
            backgroundColor: '#FFFFFF',
            border: '1px solid var(--border-light)',
            borderRadius: 'var(--radius-xl)',
            padding: '2rem 2.5rem',
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1.75rem',
            boxShadow: 'var(--shadow-sm)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
            <div
              style={{
                width: '56px',
                height: '56px',
                borderRadius: '16px',
                backgroundColor: 'var(--color-forest)',
                color: '#FFFFFF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <Sprout size={28} />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                <h3 style={{ fontSize: '1.2rem', color: 'var(--color-forest)', margin: 0 }}>Species Coverage Status</h3>
                <span
                  style={{
                    backgroundColor: 'var(--color-primary-light)',
                    color: 'var(--color-primary-dark)',
                    padding: '3px 10px',
                    borderRadius: '9999px',
                    fontSize: '0.725rem',
                    fontWeight: 700,
                    fontFamily: 'var(--font-mono)',
                  }}
                >
                  ACTIVE PRODUCTION
                </span>
              </div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.925rem', maxWidth: '600px' }}>
                Currently optimized for <strong style={{ color: 'var(--color-forest)' }}>Hass Avocado (Persea americana)</strong>.
                Climacteric suites for <span style={{ color: 'var(--color-forest)', fontWeight: 500 }}>Tomato, Banana, and Mango</span> are presently in dataset acquisition & validation phase.
              </p>
            </div>
          </div>

          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: 'var(--bg-subtle)',
              border: '1px solid var(--border-light)',
              padding: '8px 16px',
              borderRadius: '9999px',
              fontFamily: 'var(--font-mono)',
              fontSize: '0.8rem',
              color: 'var(--text-muted)',
            }}
          >
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--color-primary)' }} />
            <span>Next Release: Roma Tomato (In Research)</span>
          </div>
        </div>
      </section>

      {/* 5. SCIENTIFIC / EDUCATIONAL IMPACT CALLOUT */}
      <section className="container" style={{ paddingBottom: '5rem' }}>
        <div
          style={{
            background: 'linear-gradient(135deg, #0F291E 0%, #0A1D15 100%)',
            borderRadius: 'var(--radius-xl)',
            padding: '3.5rem 3rem',
            color: '#FFFFFF',
            position: 'relative',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-xl)',
          }}
        >
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
              gap: '2.5rem',
              alignItems: 'center',
              position: 'relative',
              zIndex: 1,
            }}
          >
            <div>
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--color-lime-glow)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '0.75rem' }}>
                Sustainable Household Impact
              </div>
              <blockquote
                style={{
                  fontSize: 'clamp(1.25rem, 2.5vw, 1.65rem)',
                  fontWeight: 700,
                  lineHeight: 1.35,
                  marginBottom: '1.5rem',
                  color: '#FFFFFF',
                }}
              >
                &ldquo;Over 40% of fresh food waste occurs in home refrigerators because timing ripeness is purely guesswork. FreshIQ transforms perishable produce management into an exact predictive science.&rdquo;
              </blockquote>

              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div
                  style={{
                    width: '40px',
                    height: '40px',
                    borderRadius: '50%',
                    backgroundColor: 'rgba(255, 255, 255, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontFamily: 'var(--font-mono)',
                    fontWeight: 700,
                    fontSize: '0.9rem',
                    color: 'var(--color-lime-glow)',
                  }}
                >
                  FQ
                </div>
                <div>
                  <div style={{ fontSize: '0.95rem', fontWeight: 600 }}>FreshIQ Biophysical Labs</div>
                  <div style={{ fontSize: '0.8rem', color: '#94A3B8' }}>Post-Harvest Degradation Research Unit</div>
                </div>
              </div>
            </div>

            {/* 6. STRONG FINAL CTA */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', justifyContent: 'center', gap: '1rem' }}>
              <Link
                to="/scan"
                className="btn-accent"
                style={{
                  backgroundColor: 'var(--color-lime)',
                  color: '#0F291E',
                  padding: '1.1rem 2.25rem',
                  fontSize: '1.1rem',
                  fontWeight: 700,
                  borderRadius: '9999px',
                  boxShadow: '0 10px 25px -5px rgba(132, 204, 22, 0.4)',
                }}
              >
                <Camera size={22} />
                <span>Launch FreshIQ Scanner</span>
                <ArrowRight size={20} />
              </Link>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.825rem', color: '#94A3B8' }}>
                <ShieldCheck size={16} style={{ color: 'var(--color-lime)' }} />
                <span>No app download required &bull; Runs instantly in your browser</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
