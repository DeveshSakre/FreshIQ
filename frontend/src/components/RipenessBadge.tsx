import React from 'react';

interface RipenessBadgeProps {
  stage: 1 | 2 | 3 | 4 | 5;
  label?: string;
  size?: 'sm' | 'md' | 'lg';
}

const STAGE_CONFIGS: Record<number, { bg: string; text: string; border: string; defaultLabel: string }> = {
  1: { bg: '#DCFCE7', text: '#166534', border: '#86EFAC', defaultLabel: 'Stage 1 — Underripe' },
  2: { bg: '#ECFCCB', text: '#3F6212', border: '#BEF264', defaultLabel: 'Stage 2 — Breaking' },
  3: { bg: '#FEF9C3', text: '#854D0E', border: '#FDE047', defaultLabel: 'Stage 3 — Ripe First Stage' },
  4: { bg: '#FFEDD5', text: '#9A3412', border: '#FDBA74', defaultLabel: 'Stage 4 — Peak Ripe (Consume Soon)' },
  5: { bg: '#FEE2E2', text: '#991B1B', border: '#FCA5A5', defaultLabel: 'Stage 5 — Overripe (Terminal)' },
};

export const RipenessBadge: React.FC<RipenessBadgeProps> = ({ stage, label, size = 'md' }) => {
  const config = STAGE_CONFIGS[stage] || STAGE_CONFIGS[1];
  const displayLabel = label || config.defaultLabel;

  const sizeStyles = {
    sm: { padding: '2px 8px', fontSize: '0.725rem' },
    md: { padding: '4px 12px', fontSize: '0.825rem' },
    lg: { padding: '6px 16px', fontSize: '0.95rem' },
  }[size];

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        backgroundColor: config.bg,
        color: config.text,
        border: `1px solid ${config.border}`,
        borderRadius: '9999px',
        fontWeight: 600,
        fontFamily: 'var(--font-mono)',
        ...sizeStyles,
      }}
    >
      <span
        style={{
          width: size === 'sm' ? '6px' : '8px',
          height: size === 'sm' ? '6px' : '8px',
          borderRadius: '50%',
          backgroundColor: config.text,
        }}
      />
      {displayLabel}
    </span>
  );
};
