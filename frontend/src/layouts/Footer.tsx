import React from 'react';
import { ShieldAlert, Cpu, Leaf, Info } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer
      style={{
        backgroundColor: '#FFFFFF',
        borderTop: '1px solid var(--border-light)',
        paddingTop: '3.5rem',
        paddingBottom: '2.5rem',
        marginTop: 'auto',
      }}
    >
      <div className="container">
        {/* Main Footer Content */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '2.5rem',
            marginBottom: '3rem',
          }}
        >
          {/* Brand & Purpose */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1rem' }}>
              <img src="/logo.svg" alt="FreshIQ Logo" style={{ height: '30px' }} />
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', lineHeight: '1.6', maxWidth: '340px' }}>
              Next-generation produce freshness assessment and dynamic What-If shelf-life estimation powered by
              MobileNetV3-Small vision and Arrhenius respiration kinetics.
            </p>
          </div>

          {/* Model Integrity & Architecture */}
          <div>
            <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.85rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Cpu size={16} style={{ color: 'var(--color-primary)' }} />
              ML Pipeline Specifications
            </h4>
            <ul style={{ listStyle: 'none', color: 'var(--text-muted)', fontSize: '0.85rem', lineHeight: '1.8' }}>
              <li><strong>Vision Model:</strong> MobileNetV3-Small (Phase 1A Checkpoint)</li>
              <li><strong>Shelf-Life Regressor:</strong> HistGradientBoosting (Phase 1B)</li>
              <li><strong>Ripening Stages:</strong> 5-Stage Hass Avocado Ordinal Classifier</li>
              <li><strong>Refrigeration:</strong> Biophysical Arrhenius Extrapolation (Q10 ≈ 2.38)</li>
            </ul>
          </div>

          {/* Responsible AI & Safety Notice */}
          <div>
            <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.85rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <ShieldAlert size={16} style={{ color: '#EAB308' }} />
              Responsible AI & Food Safety
            </h4>
            <div
              style={{
                backgroundColor: '#FFFBEB',
                border: '1px solid #FDE68A',
                borderRadius: 'var(--radius-sm)',
                padding: '0.85rem',
                fontSize: '0.825rem',
                color: '#92400E',
                lineHeight: '1.5',
              }}
            >
              <strong>Not a Food Safety Guarantee:</strong> FreshIQ estimates remaining physical shelf life until terminal overripeness.
              Always inspect produce visually and olfactorily for mold, discoloration, or off-odors before consumption.
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div
          style={{
            borderTop: '1px solid var(--border-light)',
            paddingTop: '1.5rem',
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            fontSize: '0.825rem',
            color: 'var(--text-subtle)',
          }}
        >
          <div>
            &copy; {new Date().getFullYear()} FreshIQ Platform. Built on Hass Avocado Ripening Dataset.
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
              <Leaf size={14} style={{ color: 'var(--color-primary)' }} />
              Sustainable Food Systems
            </span>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
              <Info size={14} />
              Arrhenius Kinetic Extrapolation
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
};
