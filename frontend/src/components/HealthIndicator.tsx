import React from 'react';
import { useHealth } from '../hooks/useHealth';
import { Activity, CheckCircle2, AlertTriangle, WifiOff } from 'lucide-react';

export const HealthIndicator: React.FC = () => {
  const { health, isLoading } = useHealth(20000);

  if (isLoading && !health) {
    return (
      <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
        <Activity size={14} className="animate-pulse-subtle" />
        <span>Checking API...</span>
      </div>
    );
  }

  const status = health?.status || 'offline';

  if (status === 'healthy') {
    return (
      <div
        title={`Backend API Healthy | Checkpoint: ${health?.vision_checkpoint_verified ? 'Verified (SHA-256)' : 'Unverified'}`}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '6px',
          backgroundColor: 'var(--color-primary-light)',
          color: 'var(--color-primary-dark)',
          padding: '4px 10px',
          borderRadius: '9999px',
          fontSize: '0.775rem',
          fontWeight: 600
        }}
      >
        <span
          style={{
            width: '7px',
            height: '7px',
            borderRadius: '50%',
            backgroundColor: 'var(--color-primary)',
            boxShadow: '0 0 6px var(--color-primary)'
          }}
          className="animate-pulse-subtle"
        />
        <CheckCircle2 size={13} />
        <span>API Live</span>
      </div>
    );
  }

  if (status === 'degraded') {
    return (
      <div
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '6px',
          backgroundColor: '#FEF3C7',
          color: '#B45309',
          padding: '4px 10px',
          borderRadius: '9999px',
          fontSize: '0.775rem',
          fontWeight: 600
        }}
      >
        <AlertTriangle size={13} />
        <span>API Degraded</span>
      </div>
    );
  }

  return (
    <div
      title="Unable to connect to backend on /api/health"
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        backgroundColor: '#FEE2E2',
        color: '#B91C1C',
        padding: '4px 10px',
        borderRadius: '9999px',
        fontSize: '0.775rem',
        fontWeight: 600
      }}
    >
      <WifiOff size={13} />
      <span>Backend Offline</span>
    </div>
  );
};
