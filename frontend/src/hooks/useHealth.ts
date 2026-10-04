import { useState, useEffect, useCallback } from 'react';
import { HealthResponse } from '../types/prediction';
import { checkHealth } from '../services/api';

export function useHealth(pollIntervalMs: number = 30000) {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHealth = useCallback(async () => {
    try {
      const data = await checkHealth();
      setHealth(data);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'API unreachable');
      setHealth({
        status: 'offline',
        version: 'Unknown',
        models_loaded: false,
        vision_checkpoint_verified: false,
        rul_model_loaded: false,
        feature_config_loaded: false,
        supported_produce: ['Avocado (Hass)'],
        timestamp: new Date().toISOString()
      });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHealth();
    if (pollIntervalMs > 0) {
      const interval = setInterval(fetchHealth, pollIntervalMs);
      return () => clearInterval(interval);
    }
  }, [fetchHealth, pollIntervalMs]);

  return { health, isLoading, error, refresh: fetchHealth };
}
