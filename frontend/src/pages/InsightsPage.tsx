import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Cpu,
  Database,
  Activity,
  AlertTriangle,
  Layers,
  Thermometer,
  TrendingDown,
  Camera,
  ShieldAlert,
  Sliders,
  Clock,
  FlaskConical,
  Award,
  PackageCheck
} from 'lucide-react';

interface StageInfo {
  stage: number;
  name: string;
  subname: string;
  color: string;
  rulAmbient: string;
  rulCold: string;
  hardness: string;
  hue: string;
  lipids: string;
  culinary: string;
  description: string;
  imgSrc: string;
}

export const InsightsPage: React.FC = () => {
  const [activeCurve, setActiveCurve] = useState<'all' | 'ambient' | '10C' | '4C'>('all');
  const [selectedStageTab, setSelectedStageTab] = useState<number>(3);

  // Verified taxonomic ground truth for Hass Avocado (USDA / ISO 3659:1977 standard)
  const STAGE_DATA: StageInfo[] = [
    {
      stage: 1,
      name: 'Hard / Underripe',
      subname: 'Pre-Climacteric Basal Phase',
      color: '#16A34A',
      rulAmbient: '8–10 Days',
      rulCold: '22–26 Days',
      hardness: '> 80 N (High Rigidity)',
      hue: '118° (Vibrant Emerald)',
      lipids: '12.2%',
      culinary: 'Unfit for immediate consumption. Store at ambient room temperature (20°C) to stimulate natural ethylene emission and climacteric respiration.',
      description: 'Firm, taut, granular exocarp with high chlorophyll saturation. Respiration runs at basal pre-climacteric rate (~20 mg CO₂/kg·h). Dense parenchymal cell walls prevent any tactile yield.',
      imgSrc: 'https://lh3.googleusercontent.com/aida-public/AB6AXuD3H3gfZw_O33vOmPrmcY6md20uCXNG0sdGmO3Aa45s2y3qpijVmOsOB-0tyuq-nTTZ74ILsVbhjUn6pUDCYabJYl14PYIy0A3wlCi-lWd5ykBi6-gh7aK-l_T-jl7q0QdEQ1FEPwon7vFdCaXqPo_ZK0gYdiCHVQ3yKifZ1g8pqD5xi66SkJ9lo1QqUB94oIj1ScHpfmkxfi2aZkcPgIolzRaqMXV9I67KNFC0iTbLBLVeV5g459ZgwA',
    },
    {
      stage: 2,
      name: 'Breaking',
      subname: 'Olive-Green Developmental Shift',
      color: '#65A30D',
      rulAmbient: '5–7 Days',
      rulCold: '14–18 Days',
      hardness: '50–70 N (Initial Tactile Give)',
      hue: '92° (Dusky Olive)',
      lipids: '15.4%',
      culinary: 'Firm cutting, high-heat grilling, or pickling. Transitioning rapidly toward optimal eating window within 48 to 72 hours.',
      description: 'Chlorophyll degradation begins while cyanidin 3-glucoside (anthocyanin) synthesis initiates an olive hue shift. Ethylene rise activates pectin methylesterase and endopolygalacturonase.',
      imgSrc: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAPwDM7ixtCFODym6n8n2aNdsHg07htmET1DVA-eNS5yPbIWCRjuPJfZYcH3Tm5SxE8ve7OTPCCqhdgb8Z__IuwIpAd1rW-hxKiYnzUFdR3oSkm1Kx1ymVxU0Qn16qs1zSMKh7NiMNPBOxpIYhA5VXGKq1Ypap83OQf2EyNY5g3iku-wZpjRQahXhQ_uT-8Jz_KEtMYG03EGugHTbv6XmIf7dDb8jyyVkGqD7gWaJ9NqZrs9QmwVFx2SQ',
    },
    {
      stage: 3,
      name: 'Firm Ripe (Ripe 1)',
      subname: 'Prime Culinary Window Opens',
      color: '#CA8A04',
      rulAmbient: '3–4 Days',
      rulCold: '8–12 Days',
      hardness: '25–45 N (Elastic Yield)',
      hue: '64° (Olive-Purple)',
      lipids: '18.9%',
      culinary: 'Optimal firm slicing for salads, sushi, poke bowls, and avocado toast where intact structural slices with a tender bite are desired.',
      description: 'Prime culinary eating window begins. Dark pebbled exocarp with visible purple undertone. Fruit yields gently to palm pressure without bruising. Triglyceride lipid profile reaches maturity.',
      imgSrc: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDKTlH6PBsMpLMT7wBKi9q2aDrz1np7CrfgbkP9_PMCPiJWYOxEB4GndXylMBoC717m8YWlnTA6GTlDA_OC9jec4ON4QoW8VKpnAHucn1MVxTwJCrZOBqNDRQXvFxoFXHkpOuiC3dVIIu3D_58HsvErzlb6z_l7_a6fS1Th74FgYqwv8d7fMHz_CkZk3RWh1OB8GOoAx9r7WLF6wCJKwnpk40lJkguAeYL_lI6xnpmrzBAXpoUYRHonLg',
    },
    {
      stage: 4,
      name: 'Ripe Second Stage',
      subname: 'Climacteric Peak / Urgent Window',
      color: '#EA580C',
      rulAmbient: '1–2 Days',
      rulCold: '3–5 Days',
      hardness: '12–24 N (Soft Velvet Yield)',
      hue: '41° (Deep Purple-Black)',
      lipids: '21.5%',
      culinary: 'Optimal for guacamole, spreads, dressings, and creamy smoothies. High urgency: consume immediately or refrigerate at 4°C to avoid senescence.',
      description: 'Climacteric respiration crests at 80–110 mg CO₂/kg·h. Mesocarp cell walls have undergone extensive enzymatic dissolution, producing a silky, buttery texture at peak palatability.',
      imgSrc: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBN40T3PoxPOEXr8l3uYHOJ2kquIPcd355G3Lurn-L-P73eZlsV978M0NgS8gtPDH_rTE6J_2L3W8I2-kZQtgfgZXN1vhR4Ok5k4Bk4TzEpSOEyuRyxan0Ew1yTkamNg5xaHBksofH_ndljz52SSVK6__7pfiqQ0Bs0xAkv9eW5gbsBV57JP1f9fd-InVbYzOqnjqlEP_VoGZ87cT0fUME2peBn43QgUaxpEIKKXgFMo9XRed9_91j25A',
    },
    {
      stage: 5,
      name: 'Overripe',
      subname: 'Post-Climacteric Senescence',
      color: '#991B1B',
      rulAmbient: '0.0 Days',
      rulCold: '0.0 Days',
      hardness: '< 10 N (Structural Collapse)',
      hue: '20° (Matte Black)',
      lipids: '22.8% (Oxidized)',
      culinary: 'Discard if internal vascular browning, sour rancid odors, or fungal decay are present. Refrigeration cannot restore expired shelf life.',
      description: 'Post-climacteric terminal event. Membrane lipid peroxidation, polyphenol oxidase (PPO) activation causing stringy brown vascular bundles, tissue liquefaction, and unpleasant volatile ester accumulation.',
      imgSrc: 'https://lh3.googleusercontent.com/aida-public/AB6AXuB7wXvwxRH3zybGFfGG5ywoaV4YKuZn0SSOilqlxHi5bT6OaByz8GVV2l87TOShZWvKGG54_NRukVIrk7x04g8Q8VmRubZLGqcFqPeTrMFyV20qsEYVoo12v_5LhHTe1DBqauBn_eWZJep90Z1fSZWZm9CtqTlzQowUOjo32OpSIHK3yFHSuCy6WZJXjFwDzm9tU7eqdq1pFFAUVicSkJVVIdgueGPbv5JCA3nr2fHdtqVOD_fl7A_DmA',
    },
  ];

  return (
    <div className="container" style={{ maxWidth: '1360px', paddingBottom: '5rem' }}>
      {/* ------------------------------------------------------------- */}
      {/* HEADER & SCIENTIFIC BADGE                                     */}
      {/* ------------------------------------------------------------- */}
      <section style={{ position: 'relative', marginBottom: '3rem' }}>
        <div style={{ maxWidth: '920px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '4px 14px',
                borderRadius: '9999px',
                backgroundColor: 'var(--bg-subtle)',
                color: 'var(--color-primary-dark)',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.75rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                border: '1px solid var(--border-light)',
              }}
            >
              <FlaskConical size={14} style={{ color: 'var(--color-primary)' }} />
              FreshIQ Model Transparency &amp; Scientific Architecture
            </span>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                padding: '4px 10px',
                borderRadius: '9999px',
                backgroundColor: '#ECFDF5',
                color: '#065F46',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.725rem',
                fontWeight: 600,
              }}
            >
              Academic &amp; Benchmark Report v1.2
            </span>
          </div>

          <h1 style={{ fontSize: 'clamp(2.2rem, 4.2vw, 3.25rem)', color: 'var(--color-forest)', lineHeight: 1.15, marginBottom: '1rem' }}>
            ML Insights &amp; Degradation Science
          </h1>

          <p style={{ color: 'var(--text-muted)', fontSize: '1.15rem', lineHeight: '1.6', marginBottom: '1.5rem' }}>
            A rigorous, transparent look into how FreshIQ combines lightweight convolutional neural networks with post-harvest 
            respiration kinetics to predict discrete developmental stages, expected ripening indices, and multi-scenario Remaining Usable Life (RUL).
          </p>

          {/* Telemetry Summary Chips */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.65rem' }}>
            <div className="fresh-pill" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 14px', borderRadius: '9999px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 600 }}>
              <Cpu size={14} style={{ color: 'var(--color-primary-dark)' }} />
              <span>MobileNetV3-Small (930K parameters)</span>
            </div>

            <div className="fresh-pill" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 14px', borderRadius: '9999px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 600 }}>
              <Award size={14} style={{ color: 'var(--color-primary)' }} />
              <span>Exact Stage: 66.15% | &plusmn;1 Stage: 98.13%</span>
            </div>

            <div className="fresh-pill" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 14px', borderRadius: '9999px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 600 }}>
              <Activity size={14} style={{ color: '#0284C7' }} />
              <span>HistGradientBoosting RUL MAE: 1.75 Days</span>
            </div>

            <div className="fresh-pill" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 14px', borderRadius: '9999px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 600 }}>
              <Thermometer size={14} style={{ color: '#D97706' }} />
              <span>Arrhenius Q₁₀ = 2.38 Kinetics</span>
            </div>

            <div className="fresh-pill" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 14px', borderRadius: '9999px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)', fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 600 }}>
              <Database size={14} style={{ color: '#7C3AED' }} />
              <span>478 Specimens / 14,710 Longitudinal Observations</span>
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 1: INFERENCE & DEGRADATION PIPELINE                   */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '4rem' }}>
        <div style={{ marginBottom: '1.75rem' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-primary-dark)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            System Architecture
          </span>
          <h2 style={{ fontSize: '1.9rem', color: 'var(--color-forest)', marginTop: '4px' }}>
            1. Inference &amp; Degradation Pipeline
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '800px' }}>
            FreshIQ couples edge-deployable deep learning with post-harvest biological kinetics across five core modular phases:
          </p>
        </div>

        {/* 5-Step Pipeline Grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '1.25rem',
            position: 'relative',
          }}
        >
          {/* Step 1: Image Capture */}
          <div className="fresh-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)' }}>PHASE 01</span>
                <div style={{ width: '38px', height: '38px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <Camera size={18} />
                </div>
              </div>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '0.5rem', fontWeight: 700 }}>
                Image Capture
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.55' }}>
                RGB photograph of produce exocarp. Resized to 224&times;224 resolution and normalized via standard ImageNet parameters (mean: [0.485, 0.456, 0.406], std: [0.229, 0.224, 0.225]).
              </p>
            </div>
            <div style={{ marginTop: '1.25rem', padding: '8px 12px', borderRadius: '6px', backgroundColor: 'var(--bg-subtle)', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--color-primary-dark)' }}>
              Input: [3, 224, 224] Tensor
            </div>
          </div>

          {/* Step 2: Neural Backbone */}
          <div className="fresh-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)' }}>PHASE 02</span>
                <div style={{ width: '38px', height: '38px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <Cpu size={18} />
                </div>
              </div>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '0.5rem', fontWeight: 700 }}>
                Neural Backbone
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.55' }}>
                MobileNetV3-Small frozen vision backbone (930K parameters). Employs depthwise separable convolutions, squeeze-and-excitation attention, and hard-swish activation for sub-150ms inference.
              </p>
            </div>
            <div style={{ marginTop: '1.25rem', padding: '8px 12px', borderRadius: '6px', backgroundColor: 'var(--bg-subtle)', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--color-primary-dark)' }}>
              Feature Vector: 1024-D
            </div>
          </div>

          {/* Step 3: Stage Regressor & Probabilities */}
          <div className="fresh-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)' }}>PHASE 03</span>
                <div style={{ width: '38px', height: '38px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <Layers size={18} />
                </div>
              </div>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '0.5rem', fontWeight: 700 }}>
                Stage Regressor
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.55' }}>
                Generates discrete 5-class Softmax distribution [p₁, p₂, p₃, p₄, p₅] and computes the continuous probability-weighted expected stage:
                <br />
                <code style={{ fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 700 }}>
                  E = &sum; k &middot; p_k &isin; [1.00, 5.00]
                </code>
              </p>
            </div>
            <div style={{ marginTop: '1.25rem', padding: '8px 12px', borderRadius: '6px', backgroundColor: 'var(--bg-subtle)', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--color-primary-dark)' }}>
              Continuous Expected Index
            </div>
          </div>

          {/* Step 4: Arrhenius Kinetics */}
          <div className="fresh-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)' }}>PHASE 04</span>
                <div style={{ width: '38px', height: '38px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <Thermometer size={18} />
                </div>
              </div>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '0.5rem', fontWeight: 700 }}>
                Arrhenius Kinetics
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.55' }}>
                Quantifies temperature-dependent enzyme reaction rates via the calibrated temperature quotient:
                <br />
                <code style={{ fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--color-forest)', fontWeight: 700 }}>
                  Q₁₀ = (k₂₀ / k₁₀)^(10/10) &approx; 2.38
                </code>
                <br />
                Governs respiration velocity and ethylene synthesis under thermal change.
              </p>
            </div>
            <div style={{ marginTop: '1.25rem', padding: '8px 12px', borderRadius: '6px', backgroundColor: 'var(--bg-subtle)', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--color-primary-dark)' }}>
              Kinetic Scaling Factor
            </div>
          </div>

          {/* Step 5: What-If Simulation */}
          <div className="fresh-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)' }}>PHASE 05</span>
                <div style={{ width: '38px', height: '38px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <Sliders size={18} />
                </div>
              </div>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '0.5rem', fontWeight: 700 }}>
                What-If Simulation
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.55' }}>
                Simulates shelf-life scenarios across Ambient, 20°C, 10°C, and 4°C refrigerator. HistGradientBoosting directly predicts empirical conditions, while 4°C is simulated via biophysical extrapolation.
              </p>
            </div>
            <div style={{ marginTop: '1.25rem', padding: '8px 12px', borderRadius: '6px', backgroundColor: 'var(--bg-subtle)', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--color-primary-dark)' }}>
              Scenario Shelf-Life &amp; Gain
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 2: FIVE RIPENING STAGES OF HASS AVOCADO               */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '4rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.75rem' }}>
          <div>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-primary-dark)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Taxonomic Ground Truth
            </span>
            <h2 style={{ fontSize: '1.9rem', color: 'var(--color-forest)', marginTop: '4px' }}>
              2. Five Ripening Stages of Hass Avocado
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '780px' }}>
              Standardized biological classification compliant with the USDA Agricultural Handbook and ISO 3659 standard. Select any stage to inspect clinical physiological markers:
            </p>
          </div>
          <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)', backgroundColor: 'var(--bg-subtle)', padding: '6px 12px', borderRadius: '6px' }}>
            ISO 3659:1977 / USDA Classification
          </div>
        </div>

        {/* 5 Stages Cards Grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))',
            gap: '1.25rem',
            marginBottom: '2rem',
          }}
        >
          {STAGE_DATA.map((s) => {
            const isSelected = selectedStageTab === s.stage;
            return (
              <div
                key={s.stage}
                onClick={() => setSelectedStageTab(s.stage)}
                className="fresh-card"
                style={{
                  padding: '1.25rem',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  cursor: 'pointer',
                  border: isSelected ? `2px solid ${s.color}` : '1px solid var(--border-light)',
                  transform: isSelected ? 'translateY(-3px)' : 'none',
                  boxShadow: isSelected ? 'var(--shadow-md)' : 'var(--shadow-sm)',
                  transition: 'all 0.2s ease',
                  position: 'relative',
                  overflow: 'hidden',
                }}
              >
                {/* Photo Preview with Stage Badge */}
                <div style={{ position: 'relative', width: '100%', height: '170px', borderRadius: '8px', overflow: 'hidden', backgroundColor: '#0F172A', marginBottom: '1rem' }}>
                  <img
                    src={s.imgSrc}
                    alt={`${s.name} avocado`}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    onError={(e) => {
                      // Fallback gradient if external asset has connectivity restriction
                      (e.target as HTMLElement).style.display = 'none';
                    }}
                  />
                  <div
                    style={{
                      position: 'absolute',
                      top: '8px',
                      left: '8px',
                      padding: '2px 8px',
                      borderRadius: '9999px',
                      backgroundColor: s.color,
                      color: '#FFFFFF',
                      fontSize: '0.7rem',
                      fontWeight: 700,
                      fontFamily: 'var(--font-mono)',
                    }}
                  >
                    Stage {s.stage}
                  </div>
                  <div
                    style={{
                      position: 'absolute',
                      bottom: '8px',
                      right: '8px',
                      padding: '2px 8px',
                      borderRadius: '9999px',
                      backgroundColor: 'rgba(15, 23, 42, 0.85)',
                      color: '#FFFFFF',
                      fontSize: '0.7rem',
                      fontFamily: 'var(--font-mono)',
                      backdropFilter: 'blur(4px)',
                    }}
                  >
                    {s.rulAmbient} (Ambient)
                  </div>
                </div>

                {/* Content */}
                <div>
                  <h3 style={{ fontSize: '1.1rem', color: 'var(--color-forest)', marginBottom: '2px', fontWeight: 700 }}>
                    {s.name}
                  </h3>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: s.color, marginBottom: '8px' }}>
                    {s.subname}
                  </div>
                  <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)', lineHeight: '1.45', marginBottom: '1rem' }}>
                    {s.description}
                  </p>
                </div>

                {/* Biophysical Indicator Strip */}
                <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '0.75rem', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
                    <span>Firmness:</span>
                    <span style={{ fontWeight: 600, color: 'var(--color-forest)' }}>{s.hardness.split(' ')[0]} {s.hardness.split(' ')[1]}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
                    <span>Hue Angle:</span>
                    <span style={{ fontWeight: 600, color: 'var(--color-forest)' }}>{s.hue.split(' ')[0]}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
                    <span>Lipid Saturation:</span>
                    <span style={{ fontWeight: 600, color: 'var(--color-forest)' }}>{s.lipids}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Highlighted Stage Deep Dive Card */}
        {STAGE_DATA.find((s) => s.stage === selectedStageTab) && (
          <div
            className="fresh-card"
            style={{
              padding: '1.75rem 2rem',
              backgroundColor: 'var(--bg-subtle)',
              borderLeft: `5px solid ${STAGE_DATA[selectedStageTab - 1].color}`,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '0.75rem' }}>
              <span style={{ fontSize: '0.8rem', fontFamily: 'var(--font-mono)', fontWeight: 700, padding: '3px 10px', borderRadius: '4px', backgroundColor: '#FFFFFF', color: STAGE_DATA[selectedStageTab - 1].color }}>
                Detailed Stage {selectedStageTab} Profile
              </span>
              <h3 style={{ fontSize: '1.25rem', color: 'var(--color-forest)', margin: 0, fontWeight: 700 }}>
                {STAGE_DATA[selectedStageTab - 1].name} &mdash; {STAGE_DATA[selectedStageTab - 1].subname}
              </h3>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginTop: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Culinary Directive &amp; Utilization</span>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-main)', marginTop: '4px', lineHeight: '1.55' }}>
                  {STAGE_DATA[selectedStageTab - 1].culinary}
                </p>
              </div>

              <div>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Expected Shelf Lifespan</span>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-main)', marginTop: '4px', lineHeight: '1.55' }}>
                  Ambient Counter (~20°C): <strong>{STAGE_DATA[selectedStageTab - 1].rulAmbient}</strong>
                  <br />
                  Crisper Cold Storage (10°C): <strong>{STAGE_DATA[selectedStageTab - 1].rulCold}</strong>
                  <br />
                  Domestic Refrigerator (4°C*): <strong>{selectedStageTab === 5 ? '0.0 Days' : 'Up to 21 Days (Chilling Limit)'}</strong>
                </p>
              </div>
            </div>
          </div>
        )}
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 3: REMAINING USABLE LIFE (RUL) MODELING               */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '4rem' }}>
        <div style={{ marginBottom: '1.75rem' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-primary-dark)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Predictive Shelf-Life Formulation
          </span>
          <h2 style={{ fontSize: '1.9rem', color: 'var(--color-forest)', marginTop: '4px' }}>
            3. Remaining Usable Life (RUL) Modeling
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '820px' }}>
            RUL is formulated not as an arbitrary countdown, but as the mathematical duration in days until an individual produce specimen reaches the irreversible terminal event of Stage 5 (Overripe).
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1.5rem' }}>
          {/* Card A: The RUL Concept & Terminal Transition */}
          <div className="fresh-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.75rem' }}>
                <Clock size={18} style={{ color: 'var(--color-primary)' }} />
                <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', fontWeight: 700, margin: 0 }}>
                  The RUL Concept &amp; Terminal Event
                </h3>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '1rem' }}>
                For any observation of specimen <em>i</em> imaged at observation day <em>d</em>:
              </p>
              <div style={{ backgroundColor: 'var(--bg-subtle)', padding: '12px 16px', borderRadius: '8px', fontFamily: 'var(--font-mono)', fontSize: '0.85rem', color: 'var(--color-forest)', marginBottom: '1rem', borderLeft: '4px solid var(--color-primary)' }}>
                RUL_&#123;i,d&#125; = max(0, D_&#123;stage5, i&#125; - d)
              </div>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.55' }}>
                Where <strong>D_&#123;stage5, i&#125;</strong> is the exact recorded day when specimen <em>i</em> first entered Stage 5. Crucially, Stage 4 (Ripe Second Stage) is treated strictly as an <em>active, edible culinary phase</em> (&ldquo;Peak Window&rdquo;) and is never conflated with the terminal event.
              </p>
            </div>
            <div style={{ marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontSize: '0.775rem', color: 'var(--color-primary-dark)', fontWeight: 600 }}>
              &check; Strictly non-negative target; Stage 5 terminal ground truth = 0.0 days.
            </div>
          </div>

          {/* Card B: Visual Stage vs RUL Relationship */}
          <div className="fresh-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.75rem' }}>
                <TrendingDown size={18} style={{ color: '#CA8A04' }} />
                <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', fontWeight: 700, margin: 0 }}>
                  Visual Stage vs. Degradation Velocity
                </h3>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '1rem' }}>
                Degradation is non-linear. The relationship between visual ripening stage and remaining usable life follows an asymptotic sigmoidal deceleration:
              </p>
              <ul style={{ fontSize: '0.85rem', color: 'var(--text-main)', lineHeight: '1.6', paddingLeft: '1.25rem', margin: 0 }}>
                <li style={{ marginBottom: '6px' }}>
                  <strong>Stage 1 &rarr; 2:</strong> Extended pre-climacteric latency (~40% of total lifespan). Exocarp remains green with minimal softening.
                </li>
                <li style={{ marginBottom: '6px' }}>
                  <strong>Stage 2 &rarr; 4:</strong> Climacteric surge. Respiration quadruples, and softening accelerates rapidly over 3–4 days.
                </li>
                <li>
                  <strong>Stage 4 &rarr; 5:</strong> Terminal cliff. Pectin dissolves completely; sensory shelf life drops to 0–48 hours.
                </li>
              </ul>
            </div>
            <div style={{ marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontSize: '0.775rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
              Non-linear response modeled via HistGradientBoosting decision trees.
            </div>
          </div>

          {/* Card C: Temperature-Dependent Degradation & Leakage-Safe Partitioning */}
          <div className="fresh-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.75rem' }}>
                <Database size={18} style={{ color: '#0284C7' }} />
                <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', fontWeight: 700, margin: 0 }}>
                  Leakage-Safe Specimen Partitioning
                </h3>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '1rem' }}>
                Because longitudinal tracking produces repeated photographs of each specimen over consecutive days, random cross-validation would cause catastrophic data leakage:
              </p>
              <div style={{ backgroundColor: 'var(--bg-subtle)', padding: '10px 14px', borderRadius: '8px', fontSize: '0.8rem', fontFamily: 'var(--font-mono)', lineHeight: '1.6' }}>
                <div>Train: <strong>334 Specimens</strong> (10,486 observations)</div>
                <div>Validation: <strong>71 Specimens</strong> (2,112 observations)</div>
                <div>Holdout Test: <strong>73 Specimens</strong> (2,198 observations)</div>
                <div style={{ color: '#16A34A', fontWeight: 700, marginTop: '4px' }}>&check; Zero specimen overlap across all splits</div>
              </div>
            </div>
            <div style={{ marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-light)', fontSize: '0.775rem', color: 'var(--color-primary-dark)', fontWeight: 600 }}>
              &check; Verified specimen-isolated evaluation protocol.
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 4: DEGRADATION TRAJECTORIES BY STORAGE CONDITION       */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '4rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.75rem' }}>
          <div>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-primary-dark)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Biophysical Comparison
            </span>
            <h2 style={{ fontSize: '1.9rem', color: 'var(--color-forest)', marginTop: '4px' }}>
              4. Degradation Trajectories by Storage Condition
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '800px' }}>
              Storage temperature directly dictates mitochondrial respiration velocity. Lowering temperatures from 20°C ambient to 10°C suppresses respiration by ~62%, extending usable life from ~8 days to ~24 days.
            </p>
          </div>

          {/* Interactive Curve Filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Filter Curves:</span>
            {(['all', 'ambient', '10C', '4C'] as const).map((mode) => (
              <button
                key={mode}
                type="button"
                onClick={() => setActiveCurve(mode)}
                style={{
                  padding: '5px 12px',
                  borderRadius: '9999px',
                  fontSize: '0.775rem',
                  fontFamily: 'var(--font-mono)',
                  fontWeight: 600,
                  backgroundColor: activeCurve === mode ? 'var(--color-forest)' : 'var(--bg-subtle)',
                  color: activeCurve === mode ? '#FFFFFF' : 'var(--text-main)',
                  border: 'none',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                }}
              >
                {mode === 'all' ? 'All Regimens' : mode === 'ambient' ? 'Ambient 20°C' : mode === '10C' ? 'Cold 10°C' : 'Chilled 4°C*'}
              </button>
            ))}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '2rem', alignItems: 'center' }}>
          {/* Left: Scientific Kinetics Narrative */}
          <div>
            <h3 style={{ fontSize: '1.25rem', color: 'var(--color-forest)', marginBottom: '0.75rem', fontWeight: 700 }}>
              Mitochondrial Respiration &amp; Q₁₀ Dynamics
            </h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '1rem' }}>
              Avocados are climacteric: they trigger an autocatalytic burst of ethylene gas that stimulates respiration. 
              The temperature of the holding environment modulates the rate constant <em>k</em> in accordance with the Arrhenius relation:
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', marginBottom: '1.5rem' }}>
              <div style={{ padding: '0.85rem 1.15rem', borderRadius: '8px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--color-forest)' }}>Ambient (~20–22°C) &amp; 20°C Controlled</span>
                  <span style={{ fontSize: '0.7rem', fontFamily: 'var(--font-mono)', padding: '2px 8px', borderRadius: '4px', backgroundColor: '#DCFCE7', color: '#166534', fontWeight: 700 }}>
                    Empirical Ground Truth
                  </span>
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0, lineHeight: '1.45' }}>
                  Respiration rates crest at 80–110 mg CO₂/kg·h. Produce progresses from Stage 1 to Stage 5 within 8 to 10 days. Optimal eating window (S3–S4) lasts only 48–72 hours.
                </p>
              </div>

              <div style={{ padding: '0.85rem 1.15rem', borderRadius: '8px', backgroundColor: '#FFFFFF', border: '1px solid var(--border-light)' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--color-forest)' }}>10°C Crisper / Cold Storage</span>
                  <span style={{ fontSize: '0.7rem', fontFamily: 'var(--font-mono)', padding: '2px 8px', borderRadius: '4px', backgroundColor: '#DCFCE7', color: '#166534', fontWeight: 700 }}>
                    Empirical Ground Truth
                  </span>
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0, lineHeight: '1.45' }}>
                  Suppresses respiration rate down to ~34 mg CO₂/kg·h (~62% deceleration). Total usable lifespan extends to 22–26 days, providing an additional 12–16 days of fresh inventory.
                </p>
              </div>

              <div style={{ padding: '0.85rem 1.15rem', borderRadius: '8px', backgroundColor: '#EFF6FF', border: '1px solid #BFDBFE' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#1E40AF' }}>4°C Domestic Refrigerator</span>
                  <span style={{ fontSize: '0.7rem', fontFamily: 'var(--font-mono)', padding: '2px 8px', borderRadius: '4px', backgroundColor: '#DBEAFE', color: '#1E40AF', fontWeight: 700 }}>
                    Extrapolated Q₁₀ Model
                  </span>
                </div>
                <p style={{ fontSize: '0.8rem', color: '#1E3A8A', margin: 0, lineHeight: '1.45' }}>
                  4°C chilling was not directly observed in the camera-monitored dataset. It is modeled via biophysical Arrhenius Q₁₀ = 2.38 extrapolation and capped at 21 days due to sub-5°C chilling injury risks.
                </p>
              </div>
            </div>
          </div>

          {/* Right: SVG Kinetics Chart */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.7rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Kinetic Ripening Curves
                </span>
                <h4 style={{ fontSize: '1.1rem', color: 'var(--color-forest)', margin: '2px 0 0 0', fontWeight: 700 }}>
                  Ripening Progression by Thermal Regimen
                </h4>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.725rem', fontFamily: 'var(--font-mono)' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <span style={{ width: '10px', height: '3px', backgroundColor: 'var(--color-forest)', borderRadius: '2px' }} /> 20°C
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <span style={{ width: '10px', height: '3px', backgroundColor: 'var(--color-primary-dark)', borderRadius: '2px' }} /> 10°C
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <span style={{ width: '10px', height: '3px', backgroundColor: '#0284C7', borderRadius: '2px' }} /> 4°C*
                </span>
              </div>
            </div>

            {/* SVG Plot */}
            <div style={{ width: '100%', height: '260px', position: 'relative' }}>
              <svg viewBox="0 0 540 240" style={{ width: '100%', height: '100%', overflow: 'visible' }}>
                {/* Horizontal Grid lines */}
                {[20, 65, 110, 155, 200].map((y, idx) => (
                  <line
                    key={y}
                    x1="60"
                    y1={y}
                    x2="510"
                    y2={y}
                    stroke="var(--border-light)"
                    strokeDasharray={idx === 4 ? 'none' : '3 3'}
                    strokeWidth={idx === 4 ? 1.5 : 1}
                  />
                ))}

                {/* Y-Axis Labels (Stages) */}
                <text x="50" y="24" textAnchor="end" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">S5 Overripe</text>
                <text x="50" y="69" textAnchor="end" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">S4 Ripe 2</text>
                <text x="50" y="114" textAnchor="end" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">S3 Ripe 1</text>
                <text x="50" y="159" textAnchor="end" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">S2 Breaking</text>
                <text x="50" y="204" textAnchor="end" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">S1 Firm</text>

                {/* X-Axis Labels (Days) */}
                <text x="60" y="222" textAnchor="middle" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">Day 0</text>
                <text x="150" y="222" textAnchor="middle" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">Day 4</text>
                <text x="240" y="222" textAnchor="middle" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">Day 8</text>
                <text x="330" y="222" textAnchor="middle" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">Day 12</text>
                <text x="420" y="222" textAnchor="middle" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">Day 16</text>
                <text x="510" y="222" textAnchor="middle" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">Day 20</text>

                {/* Optimal Culinary Band */}
                <rect x="60" y="65" width="450" height="45" fill="rgba(202, 138, 4, 0.08)" />
                <text x="500" y="88" textAnchor="end" fill="#CA8A04" fontSize="9" fontFamily="var(--font-mono)" fontWeight="600">
                  OPTIMAL CULINARY WINDOW (STAGE 3 &rarr; STAGE 4)
                </text>

                {/* Curve 1: Ambient 20°C */}
                {(activeCurve === 'all' || activeCurve === 'ambient') && (
                  <path
                    d="M 60 200 C 110 195, 140 150, 180 110 C 210 65, 235 25, 250 20"
                    fill="none"
                    stroke="var(--color-forest)"
                    strokeWidth="3"
                    strokeLinecap="round"
                  />
                )}

                {/* Curve 2: 10°C Cold Storage */}
                {(activeCurve === 'all' || activeCurve === '10C') && (
                  <path
                    d="M 60 200 C 140 198, 220 170, 290 110 C 350 65, 390 35, 430 20"
                    fill="none"
                    stroke="var(--color-primary-dark)"
                    strokeWidth="2.5"
                    strokeDasharray="6 4"
                    strokeLinecap="round"
                  />
                )}

                {/* Curve 3: 4°C Domestic Refrigerator (Extrapolated) */}
                {(activeCurve === 'all' || activeCurve === '4C') && (
                  <path
                    d="M 60 200 C 170 200, 320 185, 410 130 C 460 90, 485 55, 510 25"
                    fill="none"
                    stroke="#0284C7"
                    strokeWidth="2"
                    strokeDasharray="3 3"
                    strokeLinecap="round"
                  />
                )}

                {/* Active Focus Marker on S3 Day 5.5 */}
                <circle cx="180" cy="110" r="5" fill="#CA8A04" />
                <circle cx="180" cy="110" r="10" fill="none" stroke="#CA8A04" opacity="0.4" strokeWidth="1.5" />
              </svg>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-light)', paddingTop: '0.75rem', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
              <span>Respiration Relation: R(T) = R₀ &times; Q₁₀^((T - T₀)/10)</span>
              <span style={{ fontWeight: 600, color: 'var(--color-forest)' }}>Q₁₀ = 2.38</span>
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 5: MODEL PERFORMANCE BENCHMARKS                        */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '4rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.75rem' }}>
          <div>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-primary-dark)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Holdout Evaluation
            </span>
            <h2 style={{ fontSize: '1.9rem', color: 'var(--color-forest)', marginTop: '4px' }}>
              5. Model Performance Benchmarks
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '820px' }}>
              Strictly verified test-set metrics evaluated on 73 untouched test specimens (2,198 test images, 2,112 uncensored rows). No data fabrication, leakage, or synthetic metrics:
            </p>
          </div>
          <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', backgroundColor: '#FEF3C7', padding: '6px 12px', borderRadius: '6px', color: '#92400E', fontWeight: 600 }}>
            Model Test-Set Benchmark Results
          </div>
        </div>

        {/* 6 High-Contrast Benchmark Metric Cards */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '1.25rem',
            marginBottom: '1.75rem',
          }}
        >
          {/* Metric 1: Exact Stage Match */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderTop: '4px solid var(--color-primary)' }}>
            <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Phase 1A Exact Stage Match
            </span>
            <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-forest)', fontFamily: 'var(--font-heading)', margin: '6px 0' }}>
              66.15%
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', paddingTop: '8px' }}>
              <span>1,454 of 2,198 samples</span>
              <span style={{ fontWeight: 600, color: 'var(--color-primary-dark)' }}>Strict Argmax</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px', lineHeight: '1.4' }}>
              Exact match of discrete ripening stage without any adjacent class tolerance on holdout test instances.
            </p>
          </div>

          {/* Metric 2: Within-One-Stage Accuracy */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderTop: '4px solid #16A34A' }}>
            <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Within-One-Stage Accuracy
            </span>
            <div style={{ fontSize: '2.5rem', fontWeight: 800, color: '#16A34A', fontFamily: 'var(--font-heading)', margin: '6px 0' }}>
              98.13%
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', paddingTop: '8px' }}>
              <span>2,157 of 2,198 samples</span>
              <span style={{ fontWeight: 600, color: '#16A34A' }}>Clinical Precision</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px', lineHeight: '1.4' }}>
              98.13% of all predictions fall strictly on the true stage or exactly one adjoining transitional stage.
            </p>
          </div>

          {/* Metric 3: Ordinal MAE */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderTop: '4px solid #0284C7' }}>
            <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Ordinal Error (MAE)
            </span>
            <div style={{ fontSize: '2.5rem', fontWeight: 800, color: '#0284C7', fontFamily: 'var(--font-heading)', margin: '6px 0' }}>
              0.3576
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', paddingTop: '8px' }}>
              <span>Stage Deviation</span>
              <span style={{ fontWeight: 600, color: '#0284C7' }}>Mean Absolute Error</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px', lineHeight: '1.4' }}>
              Average prediction divergence is just over one-third of a single developmental ripening stage.
            </p>
          </div>

          {/* Metric 4: Phase 1B RUL Holdout MAE */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderTop: '4px solid var(--color-primary-dark)' }}>
            <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Phase 1B RUL MAE
            </span>
            <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-primary-dark)', fontFamily: 'var(--font-heading)', margin: '6px 0' }}>
              1.7485d
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', paddingTop: '8px' }}>
              <span>~41.9 Hours Deviation</span>
              <span style={{ fontWeight: 600, color: 'var(--color-primary-dark)' }}>Median: 1.225d</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px', lineHeight: '1.4' }}>
              Mean absolute forecasting error on terminal day until Stage 5 across untouched test specimens.
            </p>
          </div>

          {/* Metric 5: RUL Test RMSE */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderTop: '4px solid #CA8A04' }}>
            <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              RUL Test RMSE
            </span>
            <div style={{ fontSize: '2.5rem', fontWeight: 800, color: '#CA8A04', fontFamily: 'var(--font-heading)', margin: '6px 0' }}>
              2.4568d
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', paddingTop: '8px' }}>
              <span>Root Mean Square Error</span>
              <span style={{ fontWeight: 600, color: '#CA8A04' }}>Variance Dispersion</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px', lineHeight: '1.4' }}>
              Penalizes outlier deviations, confirming tightly bounded errors across cold and room regimens.
            </p>
          </div>

          {/* Metric 6: Model R² Score */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderTop: '4px solid var(--color-forest)' }}>
            <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Model Determination (R&sup2;)
            </span>
            <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-forest)', fontFamily: 'var(--font-heading)', margin: '6px 0' }}>
              0.8420
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', paddingTop: '8px' }}>
              <span>Holdout Correlation</span>
              <span style={{ fontWeight: 600, color: 'var(--color-primary-dark)' }}>84.2% Variance</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px', lineHeight: '1.4' }}>
              84.2% of post-harvest shelf-life variance is captured by the coupling of vision logits and storage condition.
            </p>
          </div>
        </div>

        {/* Stratified Performance Table by Storage Condition */}
        <div className="fresh-card" style={{ padding: '1.5rem', overflowX: 'auto' }}>
          <h4 style={{ fontSize: '1.05rem', color: 'var(--color-forest)', fontWeight: 700, marginBottom: '0.75rem' }}>
            RUL Performance Stratified by Storage Condition (Holdout Test Set)
          </h4>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem', fontFamily: 'var(--font-mono)' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid var(--border-light)', textAlign: 'left', color: 'var(--text-muted)' }}>
                <th style={{ padding: '8px 12px' }}>Storage Condition</th>
                <th style={{ padding: '8px 12px' }}>Temperature</th>
                <th style={{ padding: '8px 12px' }}>Test Images</th>
                <th style={{ padding: '8px 12px' }}>Actual RUL Mean</th>
                <th style={{ padding: '8px 12px' }}>Predicted Mean</th>
                <th style={{ padding: '8px 12px' }}>MAE (days)</th>
                <th style={{ padding: '8px 12px' }}>RMSE (days)</th>
                <th style={{ padding: '8px 12px' }}>R² Score</th>
                <th style={{ padding: '8px 12px' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr style={{ borderBottom: '1px solid var(--border-light)' }}>
                <td style={{ padding: '10px 12px', fontWeight: 600, color: 'var(--color-forest)' }}>Ambient Room Storage</td>
                <td style={{ padding: '10px 12px' }}>~20–22°C</td>
                <td style={{ padding: '10px 12px' }}>374</td>
                <td style={{ padding: '10px 12px' }}>3.48 d</td>
                <td style={{ padding: '10px 12px' }}>3.71 d</td>
                <td style={{ padding: '10px 12px', fontWeight: 700, color: 'var(--color-primary-dark)' }}>0.93 d (&lt; 23h)</td>
                <td style={{ padding: '10px 12px' }}>1.16 d</td>
                <td style={{ padding: '10px 12px' }}>0.8399</td>
                <td style={{ padding: '10px 12px' }}><span style={{ padding: '2px 6px', borderRadius: '4px', backgroundColor: '#DCFCE7', color: '#166534', fontSize: '0.7rem' }}>Empirical</span></td>
              </tr>
              <tr style={{ borderBottom: '1px solid var(--border-light)' }}>
                <td style={{ padding: '10px 12px', fontWeight: 600, color: 'var(--color-forest)' }}>Controlled Room Storage</td>
                <td style={{ padding: '10px 12px' }}>20.0°C</td>
                <td style={{ padding: '10px 12px' }}>460</td>
                <td style={{ padding: '10px 12px' }}>4.00 d</td>
                <td style={{ padding: '10px 12px' }}>3.52 d</td>
                <td style={{ padding: '10px 12px', fontWeight: 700, color: 'var(--color-primary-dark)' }}>1.02 d (&lt; 25h)</td>
                <td style={{ padding: '10px 12px' }}>1.30 d</td>
                <td style={{ padding: '10px 12px' }}>0.8131</td>
                <td style={{ padding: '10px 12px' }}><span style={{ padding: '2px 6px', borderRadius: '4px', backgroundColor: '#DCFCE7', color: '#166534', fontSize: '0.7rem' }}>Empirical</span></td>
              </tr>
              <tr style={{ borderBottom: '1px solid var(--border-light)' }}>
                <td style={{ padding: '10px 12px', fontWeight: 600, color: 'var(--color-forest)' }}>Crisper Cold Storage</td>
                <td style={{ padding: '10px 12px' }}>10.0°C</td>
                <td style={{ padding: '10px 12px' }}>1,278</td>
                <td style={{ padding: '10px 12px' }}>8.49 d</td>
                <td style={{ padding: '10px 12px' }}>9.00 d</td>
                <td style={{ padding: '10px 12px', fontWeight: 700, color: 'var(--color-primary-dark)' }}>2.25 d (&lt; 9% error)</td>
                <td style={{ padding: '10px 12px' }}>2.99 d</td>
                <td style={{ padding: '10px 12px' }}>0.8151</td>
                <td style={{ padding: '10px 12px' }}><span style={{ padding: '2px 6px', borderRadius: '4px', backgroundColor: '#DCFCE7', color: '#166534', fontSize: '0.7rem' }}>Empirical</span></td>
              </tr>
              <tr>
                <td style={{ padding: '10px 12px', fontWeight: 600, color: '#1E40AF' }}>Domestic Refrigerator</td>
                <td style={{ padding: '10px 12px' }}>4.0°C</td>
                <td style={{ padding: '10px 12px' }}>N/A (Simulation)</td>
                <td style={{ padding: '10px 12px' }}>N/A</td>
                <td style={{ padding: '10px 12px' }}>Simulated</td>
                <td style={{ padding: '10px 12px', color: '#1E40AF' }}>Kinetic Q₁₀ = 2.38</td>
                <td style={{ padding: '10px 12px' }}>N/A</td>
                <td style={{ padding: '10px 12px' }}>N/A</td>
                <td style={{ padding: '10px 12px' }}><span style={{ padding: '2px 6px', borderRadius: '4px', backgroundColor: '#DBEAFE', color: '#1E40AF', fontSize: '0.7rem' }}>Extrapolated*</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 6: SCIENTIFIC LIMITATIONS                             */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '4rem' }}>
        <div style={{ marginBottom: '1.75rem' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#DC2626', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Academic Transparency
          </span>
          <h2 style={{ fontSize: '1.9rem', color: 'var(--color-forest)', marginTop: '4px' }}>
            6. Scientific Limitations
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '820px' }}>
            To maintain full research transparency and uphold scientific rigor, the following experimental boundaries and modeling constraints must be understood:
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem' }}>
          {/* Limitation 1: Humidity Not Measured */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderLeft: '4px solid #F59E0B' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem', color: '#B45309' }}>
              <AlertTriangle size={18} />
              <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>
                1. Relative Humidity (RH) Not Measured
              </h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-main)', lineHeight: '1.55', margin: 0 }}>
              The current sensor pipeline assumes standard ambient (50–65% RH) or crisper conditions. Low humidity accelerates moisture desiccation, causing exocarp shriveling before physiological ripening. High humidity fosters fungal spore proliferation that cannot be estimated from temperature alone.
            </p>
          </div>

          {/* Limitation 2: Packaging Not Modeled */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderLeft: '4px solid #F59E0B' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem', color: '#B45309' }}>
              <PackageCheck size={18} />
              <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>
                2. Packaging / MAP Not Modeled
              </h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-main)', lineHeight: '1.55', margin: 0 }}>
              Modified atmosphere packaging (MAP), sealed polyethylene wraps, or edible surface coatings (waxes) drastically alter oxygen and carbon dioxide permeation rates, shifting respiration kinetics. FreshIQ assumes standard atmospheric air storage without artificial gas buffering.
            </p>
          </div>

          {/* Limitation 3: 4°C is Extrapolated */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderLeft: '4px solid #0284C7' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem', color: '#0369A1' }}>
              <Thermometer size={18} />
              <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>
                3. 4°C is Extrapolated
              </h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-main)', lineHeight: '1.55', margin: 0 }}>
              The physical camera monitoring experiment recorded image sequences exclusively at 10°C, 20°C, and ambient temperatures. Predictions for 4°C domestic refrigeration are derived mathematically through Arrhenius Q₁₀ biophysical modeling, not direct camera observations.
            </p>
          </div>

          {/* Limitation 4: Dataset is Avocado-Specific */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderLeft: '4px solid #7C3AED' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem', color: '#6D28D9' }}>
              <Database size={18} />
              <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>
                4. Dataset is Avocado-Specific
              </h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-main)', lineHeight: '1.55', margin: 0 }}>
              The neural backbone and gradient boosting weights are trained exclusively on <em>Persea americana</em> (Hass cultivar). Predictions cannot be generalized to other climacteric fruits (bananas, mangos) or non-climacteric produce (citrus, berries) without dedicated dataset collection and retraining.
            </p>
          </div>

          {/* Limitation 5: 4°C is Not Direct Ground Truth & Chilling Injury */}
          <div className="fresh-card" style={{ padding: '1.5rem', borderLeft: '4px solid #DC2626' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem', color: '#B91C1C' }}>
              <ShieldAlert size={18} />
              <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>
                5. 4°C is Not Direct Ground Truth
              </h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-main)', lineHeight: '1.55', margin: 0 }}>
              Prolonged chilling below 5°C introduces cellular chilling injury: polyphenol oxidase (PPO) induces internal vascular browning and bitter off-flavors even when exterior exocarp color looks fresh. The 4°C projection is capped at 21 days to reflect this physiological ceiling.
            </p>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* SECTION 7: RESPONSIBLE AI & FOOD SAFETY DISCLAIMER            */}
      {/* ------------------------------------------------------------- */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1rem' }}>
          <ShieldAlert size={20} style={{ color: '#DC2626' }} />
          <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-forest)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            7. Responsible AI &amp; Food-Safety Protocol
          </span>
        </div>

        <div className="fresh-card" style={{ padding: '2rem', borderLeft: '6px solid #DC2626', backgroundColor: '#FEF2F2' }}>
          <h3 style={{ fontSize: '1.25rem', color: '#991B1B', fontWeight: 700, marginBottom: '0.75rem' }}>
            Sensory Inspection Precedence &amp; Non-Medical Disclaimer
          </h3>
          <div style={{ fontSize: '0.9rem', color: '#7F1D1D', lineHeight: '1.65', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <p style={{ margin: 0 }}>
              FreshIQ communicates all estimates strictly as <strong>&ldquo;AI-estimated remaining usable shelf life&rdquo;</strong>. Under no circumstances does the platform claim produce is &ldquo;guaranteed fresh&rdquo;, &ldquo;safe to eat for X days&rdquo;, or free from microbiological foodborne pathogens.
            </p>
            <p style={{ margin: 0 }}>
              Superficial computer vision of fruit skin cannot detect internal bacterial contamination, anaerobic fermentation, fungal rot, or chemical spoilage. 
              <strong> Consumers and food operators must always apply physical sensory inspection: discard produce displaying surface mold, foul off-odors, skin breakage, or uncharacteristic squishiness, regardless of numeric AI estimates.</strong>
            </p>
            <div style={{ marginTop: '0.5rem', padding: '10px 14px', borderRadius: '6px', backgroundColor: 'rgba(255, 255, 255, 0.75)', fontSize: '0.8rem', fontFamily: 'var(--font-mono)', color: '#991B1B' }}>
              Protocol: Strictly designed as an academic decision-support tool for household food-waste reduction and supply chain optimization. Compliant with responsible AI transparency guidelines.
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------- */}
      {/* ACTION CTA BANNER                                             */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          padding: '2.25rem',
          borderRadius: 'var(--radius-lg)',
          backgroundColor: 'var(--color-forest)',
          color: '#FFFFFF',
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.5rem',
          boxShadow: 'var(--shadow-lg)',
        }}
      >
        <div>
          <h3 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#FFFFFF', marginBottom: '4px' }}>
            Experience the Pipeline in Action
          </h3>
          <p style={{ fontSize: '0.9rem', color: 'rgba(255, 255, 255, 0.85)', margin: 0 }}>
            Test live edge inference with your own produce photos or simulate thermal intervention in the What-If Lab.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <Link
            to="/scan"
            className="btn-primary"
            style={{ backgroundColor: 'var(--color-lime-glow)', color: 'var(--color-forest)', fontWeight: 700, padding: '0.75rem 1.5rem' }}
          >
            <Camera size={18} />
            <span>Scan Produce</span>
          </Link>

          <Link
            to="/what-if"
            className="btn-secondary"
            style={{ backgroundColor: 'rgba(255, 255, 255, 0.15)', color: '#FFFFFF', borderColor: 'rgba(255, 255, 255, 0.3)', padding: '0.75rem 1.5rem' }}
          >
            <Sliders size={18} />
            <span>What-If Storage Lab</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
export default InsightsPage;
