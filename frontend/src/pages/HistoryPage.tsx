import React, { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Search,
  Trash2,
  Camera,
  Eye,
  Sliders,
  CheckCircle2,
  AlertTriangle,
  Leaf,
  Clock,
  RefreshCw,
  Loader2,
  Thermometer,
  Package,
  Activity,
  BarChart3,
  X
} from 'lucide-react';
import { useScan } from '../context/ScanContext';
import { RipenessBadge } from '../components/RipenessBadge';
import { predictProduce, ApiError } from '../services/api';

type FilterCategory = 'all' | 'urgent' | 'chilled' | 'fresh' | 'stage5';
type SortOption = 'newest' | 'oldest' | 'rul_asc' | 'rul_desc' | 'confidence_desc';

const STAGE_COLORS: Record<number, string> = {
  1: '#16A34A',
  2: '#65A30D',
  3: '#CA8A04',
  4: '#EA580C',
  5: '#991B1B',
};

export const HistoryPage: React.FC = () => {
  const navigate = useNavigate();
  const { history, clearHistory, removeFromHistory, setCurrentScan } = useScan();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState<FilterCategory>('all');
  const [selectedSort, setSelectedSort] = useState<SortOption>('newest');
  const [showClearConfirm, setShowClearConfirm] = useState(false);
  const [isGeneratingDemo, setIsGeneratingDemo] = useState(false);
  const [demoError, setDemoError] = useState<string | null>(null);

  // Compute telemetry metrics from real history data
  const telemetry = useMemo(() => {
    const total = history.length;
    let urgent = 0;
    let chilled = 0;
    let fresh = 0;
    let stage5 = 0;

    history.forEach((item) => {
      const stage = item.result.ripeness.predicted_ripening_stage;
      const rul = item.result.scenarios.ambient.estimated_rul_days;
      if (stage === 5) {
        stage5++;
      } else if (stage === 4 || rul <= 2.0) {
        urgent++;
      } else {
        fresh++;
      }
      if (item.result.refrigeration_extension_gain_days > 0) {
        chilled++;
      }
    });

    const wasteSaved = (total * 3.25).toFixed(2);
    const co2Avoided = (total * 0.45).toFixed(1);

    return { total, urgent, chilled, fresh, stage5, wasteSaved, co2Avoided };
  }, [history]);

  // Filtered and sorted items
  const processedItems = useMemo(() => {
    const filtered = history.filter((item) => {
      const res = item.result;
      const stage = res.ripeness.predicted_ripening_stage;
      const rul = res.scenarios.ambient.estimated_rul_days;

      // Filter by category
      if (selectedFilter === 'urgent' && (stage !== 4 && rul > 2.0)) {
        return false;
      }
      if (selectedFilter === 'stage5' && stage !== 5) {
        return false;
      }
      if (selectedFilter === 'chilled' && res.refrigeration_extension_gain_days <= 0) {
        return false;
      }
      if (selectedFilter === 'fresh' && (stage >= 4 || rul <= 2.0)) {
        return false;
      }

      // Filter by search query
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim();
        const matchesName = res.item_name.toLowerCase().includes(query);
        const matchesStage = res.ripeness.stage_label.toLowerCase().includes(query);
        const matchesId = item.id.toLowerCase().includes(query);
        const matchesCond = (item.storageCondition || '').toLowerCase().includes(query);
        return matchesName || matchesStage || matchesId || matchesCond;
      }

      return true;
    });

    // Sort items
    return filtered.sort((a, b) => {
      if (selectedSort === 'newest') {
        return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
      }
      if (selectedSort === 'oldest') {
        return new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime();
      }
      if (selectedSort === 'rul_asc') {
        return a.result.scenarios.ambient.estimated_rul_days - b.result.scenarios.ambient.estimated_rul_days;
      }
      if (selectedSort === 'rul_desc') {
        return b.result.scenarios.ambient.estimated_rul_days - a.result.scenarios.ambient.estimated_rul_days;
      }
      if (selectedSort === 'confidence_desc') {
        return b.result.ripeness.confidence - a.result.ripeness.confidence;
      }
      return 0;
    });
  }, [history, selectedFilter, searchQuery, selectedSort]);

  // Helper to format storage condition label
  const getStorageConditionLabel = (cond?: string) => {
    if (!cond) return 'Ambient (~20°C)';
    const lower = cond.toLowerCase();
    if (lower.includes('10')) return 'Cold Storage (10°C)';
    if (lower.includes('4')) return 'Refrigerator (4°C)';
    if (lower.includes('20')) return 'Controlled (20°C)';
    return 'Ambient Counter (~20°C)';
  };

  // Helper to open analysis for an item
  const handleViewAnalysis = (item: typeof history[0]) => {
    setCurrentScan(item.result, item.thumbnailUrl, item.storageCondition);
    navigate('/analysis');
  };

  // Helper to open comparison for an item
  const handleCompare = (item: typeof history[0]) => {
    setCurrentScan(item.result, item.thumbnailUrl, item.storageCondition);
    navigate('/comparison');
  };

  // Helper to run live sample scan and add to history
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

        const demoFile = new File([blob], `sample_avocado_stage_${sampleStage}.jpg`, { type: 'image/jpeg' });
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

  return (
    <div className="container" style={{ maxWidth: '1360px', paddingBottom: '4rem' }}>
      {/* ------------------------------------------------------------- */}
      {/* 1. TOP TITLE & GLOBAL ACTION SECTION                          */}
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
        <div style={{ maxWidth: '780px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '3px 10px',
                borderRadius: '9999px',
                backgroundColor: 'var(--bg-subtle)',
                color: 'var(--text-muted)',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.725rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.04em',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-primary)' }} />
              Pantry Inventory &amp; Archive
            </span>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                padding: '3px 10px',
                borderRadius: '9999px',
                backgroundColor: '#F1F5F9',
                color: '#475569',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.7rem',
                fontWeight: 600,
              }}
            >
              Browser-Local Storage (localStorage) &bull; No Server DB
            </span>
          </div>

          <h1 style={{ fontSize: 'clamp(2rem, 3.5vw, 2.75rem)', color: 'var(--color-forest)', lineHeight: 1.15, marginBottom: '0.5rem' }}>
            Scan History &amp; Active Produce
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', lineHeight: '1.5' }}>
            Track ripening progression, microclimate storage positions, and AI consumption countdowns for all registered produce.
          </p>
        </div>

        {/* Global Action Buttons */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          {history.length > 0 && (
            <button
              type="button"
              onClick={() => setShowClearConfirm(true)}
              className="btn-secondary"
              style={{ padding: '0.65rem 1.15rem', fontSize: '0.85rem', color: '#DC2626', borderColor: '#FCA5A5' }}
            >
              <Trash2 size={16} />
              <span>Clear History ({history.length})</span>
            </button>
          )}

          <Link
            to="/scan"
            className="btn-primary"
            style={{ padding: '0.65rem 1.35rem', fontSize: '0.875rem' }}
          >
            <Camera size={18} />
            <span>Scan New Item</span>
          </Link>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 2. LIVE KPI TELEMETRY BAND                                    */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1.25rem',
          marginBottom: '2rem',
        }}
      >
        {/* KPI 1: Active Inventory */}
        <div className="fresh-card" style={{ padding: '1.25rem', position: 'relative', overflow: 'hidden' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Active Inventory
            </span>
            <Package size={18} style={{ color: 'var(--color-primary)' }} />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--color-forest)', fontFamily: 'var(--font-heading)' }}>
            {telemetry.total} Items
          </div>
          <div style={{ fontSize: '0.775rem', color: 'var(--text-muted)', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Activity size={13} style={{ color: 'var(--color-primary-dark)' }} />
            <span>Real-time local storage records</span>
          </div>
          <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '3px', backgroundColor: 'var(--color-primary)' }} />
        </div>

        {/* KPI 2: Urgent Peak Window */}
        <div className="fresh-card" style={{ padding: '1.25rem', position: 'relative', overflow: 'hidden' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Urgent Peak Window
            </span>
            <AlertTriangle size={18} style={{ color: '#DC2626' }} />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: telemetry.urgent > 0 ? '#DC2626' : 'var(--color-forest)', fontFamily: 'var(--font-heading)' }}>
            {telemetry.urgent} Alert{telemetry.urgent === 1 ? '' : 's'}
          </div>
          <div style={{ fontSize: '0.775rem', color: 'var(--text-muted)', marginTop: '4px' }}>
            Requires consumption &le; 48h
          </div>
          <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '3px', backgroundColor: '#DC2626' }} />
        </div>

        {/* KPI 3: Lifetime Scans */}
        <div className="fresh-card" style={{ padding: '1.25rem', position: 'relative', overflow: 'hidden' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Lifetime Scans
            </span>
            <BarChart3 size={18} style={{ color: 'var(--color-forest)' }} />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--color-forest)', fontFamily: 'var(--font-heading)' }}>
            {telemetry.total} Scans
          </div>
          <div style={{ fontSize: '0.775rem', color: 'var(--text-muted)', marginTop: '4px' }}>
            Avocado Model v1.2 (MobileNetV3)
          </div>
          <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '3px', backgroundColor: 'var(--border-light)' }} />
        </div>

        {/* KPI 4: Estimated Waste Prevented */}
        <div className="fresh-card" style={{ padding: '1.25rem', position: 'relative', overflow: 'hidden' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Estimated Waste Prevented
            </span>
            <Leaf size={18} style={{ color: 'var(--color-primary-dark)' }} />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--color-primary-dark)', fontFamily: 'var(--font-heading)' }}>
            ${telemetry.wasteSaved}
          </div>
          <div style={{ fontSize: '0.775rem', color: 'var(--color-primary-dark)', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <CheckCircle2 size={13} />
            <span>{telemetry.co2Avoided} kg CO₂e avoided</span>
          </div>
          <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '3px', backgroundColor: 'var(--color-lime-glow)' }} />
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 3. FILTER CONTROLS & SEARCH TOOLBAR                           */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
          padding: '0.75rem 1rem',
          backgroundColor: 'var(--bg-subtle)',
          borderRadius: 'var(--radius-md)',
          marginBottom: '1.5rem',
        }}
      >
        {/* Search Box */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: '#FFFFFF',
            borderRadius: '9999px',
            padding: '8px 16px',
            boxShadow: 'var(--shadow-sm)',
            flex: '1 1 260px',
            maxWidth: '480px',
          }}
        >
          <Search size={18} style={{ color: 'var(--text-muted)' }} />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by batch ID, stage, or produce..."
            style={{
              border: 'none',
              outline: 'none',
              width: '100%',
              fontSize: '0.875rem',
              color: 'var(--text-main)',
              backgroundColor: 'transparent',
            }}
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery('')}
              style={{ fontSize: '0.75rem', color: 'var(--text-muted)', cursor: 'pointer' }}
            >
              Clear
            </button>
          )}
        </div>

        {/* Filter Chips */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
          {[
            { key: 'all', label: `All (${telemetry.total})` },
            { key: 'urgent', label: `Needs Attention (${telemetry.urgent})` },
            { key: 'chilled', label: `Chilled (${telemetry.chilled})` },
            { key: 'fresh', label: `Fresh & Firm (${telemetry.fresh})` },
            { key: 'stage5', label: `Overripe (${telemetry.stage5})` },
          ].map((cat) => {
            const isActive = selectedFilter === cat.key;
            return (
              <button
                key={cat.key}
                type="button"
                onClick={() => setSelectedFilter(cat.key as FilterCategory)}
                style={{
                  padding: '6px 14px',
                  borderRadius: '9999px',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  backgroundColor: isActive ? 'var(--color-forest)' : '#FFFFFF',
                  color: isActive ? '#FFFFFF' : 'var(--text-main)',
                  boxShadow: isActive ? 'var(--shadow-sm)' : 'none',
                  border: isActive ? 'none' : '1px solid var(--border-light)',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                }}
              >
                {cat.label}
              </button>
            );
          })}
        </div>

        {/* Sort Selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Sort:</span>
          <select
            value={selectedSort}
            onChange={(e) => setSelectedSort(e.target.value as SortOption)}
            style={{
              padding: '6px 12px',
              borderRadius: '9999px',
              fontSize: '0.8rem',
              fontWeight: 600,
              backgroundColor: '#FFFFFF',
              color: 'var(--color-forest)',
              border: '1px solid var(--border-light)',
              cursor: 'pointer',
              outline: 'none',
              boxShadow: 'var(--shadow-sm)',
            }}
          >
            <option value="newest">Newest Scans First</option>
            <option value="oldest">Oldest Scans First</option>
            <option value="rul_asc">RUL: Shortest First (Urgent)</option>
            <option value="rul_desc">RUL: Longest First (Holding)</option>
            <option value="confidence_desc">Confidence: Highest First</option>
          </select>
        </div>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 4. MAIN CONTENT AREA: CARDS STREAM OR EMPTY STATE             */}
      {/* ------------------------------------------------------------- */}
      {history.length === 0 ? (
        /* Empty State */
        <div className="fresh-card" style={{ padding: '4rem 2rem', textAlign: 'center' }}>
          <div
            style={{
              width: '72px',
              height: '72px',
              borderRadius: '50%',
              backgroundColor: 'var(--bg-subtle)',
              color: 'var(--color-primary-dark)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem',
            }}
          >
            <Leaf size={36} />
          </div>

          <h2 style={{ fontSize: '1.75rem', color: 'var(--color-forest)', marginBottom: '0.5rem' }}>
            No Produce Scanned Yet
          </h2>
          <p style={{ color: 'var(--text-muted)', maxWidth: '480px', margin: '0 auto 2rem', lineHeight: '1.6' }}>
            Start tracking your fruit and vegetables to get real-time ripeness diagnostics, decay modeling, and eliminate household food waste.
          </p>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <Link to="/scan" className="btn-primary" style={{ padding: '0.85rem 1.75rem' }}>
              <Camera size={18} />
              <span>Scan Your First Hass Avocado</span>
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
                  <span>Scanning Sample Avocado...</span>
                </>
              ) : (
                <>
                  <RefreshCw size={18} />
                  <span>Add Sample Avocado Scan</span>
                </>
              )}
            </button>
          </div>

          {demoError && (
            <div style={{ maxWidth: '480px', margin: '1.5rem auto 0', padding: '0.75rem', backgroundColor: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', borderRadius: '8px', fontSize: '0.85rem' }}>
              {demoError}
            </div>
          )}
        </div>
      ) : processedItems.length === 0 ? (
        /* No matches for current filter/search */
        <div className="fresh-card" style={{ padding: '3rem 2rem', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', marginBottom: '1rem' }}>
            No produce records matched your search query or selected filter.
          </p>
          <button
            type="button"
            onClick={() => {
              setSearchQuery('');
              setSelectedFilter('all');
            }}
            className="btn-secondary"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        /* Active Cards Stream */
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {processedItems.map((item) => {
            const res = item.result;
            const stage = res.ripeness.predicted_ripening_stage;
            const stageLabel = res.ripeness.stage_label;
            const confidence = (res.ripeness.confidence * 100).toFixed(1);
            const ambientRul = res.scenarios.ambient.estimated_rul_days;
            const isUrgent = stage >= 4 || ambientRul <= 2.0;
            const isTerminal = stage === 5;
            const formattedDate = new Date(item.timestamp).toLocaleString(undefined, {
              dateStyle: 'medium',
              timeStyle: 'short',
            });

            // Status border indicator color
            const accentColor = isTerminal ? '#991B1B' : isUrgent ? '#EA580C' : '#16A34A';

            return (
              <div
                key={item.id}
                className="fresh-card"
                style={{
                  padding: '1.25rem 1.5rem',
                  display: 'flex',
                  flexDirection: 'row',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '1.25rem',
                  borderLeft: `5px solid ${accentColor}`,
                  position: 'relative',
                }}
              >
                {/* Left: Thumbnail & Specimen Details */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flex: '1 1 360px' }}>
                  {/* Thumbnail */}
                  <div
                    style={{
                      width: '84px',
                      height: '84px',
                      borderRadius: '12px',
                      overflow: 'hidden',
                      backgroundColor: '#0F172A',
                      flexShrink: 0,
                      boxShadow: 'var(--shadow-sm)',
                    }}
                  >
                    {item.thumbnailUrl ? (
                      <img
                        src={item.thumbnailUrl}
                        alt="Scanned produce thumbnail"
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                      />
                    ) : (
                      <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94A3B8' }}>
                        <Camera size={28} />
                      </div>
                    )}
                  </div>

                  {/* Metadata */}
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '4px' }}>
                      <span style={{ fontWeight: 700, fontSize: '1.1rem', color: 'var(--color-forest)' }}>
                        {res.item_name}
                      </span>
                      <span
                        style={{
                          fontFamily: 'var(--font-mono)',
                          fontSize: '0.7rem',
                          backgroundColor: 'var(--bg-subtle)',
                          padding: '1px 6px',
                          borderRadius: '4px',
                          color: 'var(--text-muted)',
                        }}
                      >
                        #{item.id.slice(-6).toUpperCase()}
                      </span>
                      <RipenessBadge stage={stage} label={`Stage ${stage}: ${stageLabel}`} size="sm" />
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.775rem', color: 'var(--text-muted)', flexWrap: 'wrap', marginBottom: '6px' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Clock size={13} /> {formattedDate}
                      </span>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px', backgroundColor: '#F1F5F9', padding: '2px 8px', borderRadius: '4px', color: '#334155', fontWeight: 600 }}>
                        <Thermometer size={13} /> {getStorageConditionLabel(item.storageCondition)}
                      </span>
                      <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--color-primary-dark)' }}>
                        {confidence}% Confidence
                      </span>
                      <span
                        style={{
                          backgroundColor: isTerminal ? '#FEE2E2' : isUrgent ? '#FFEDD5' : '#DCFCE7',
                          color: isTerminal ? '#991B1B' : isUrgent ? '#C2410C' : '#15803D',
                          padding: '2px 8px',
                          borderRadius: '9999px',
                          fontWeight: 700,
                          fontSize: '0.7rem',
                        }}
                      >
                        {isTerminal ? 'Overripe (Senescent)' : isUrgent ? 'Consume Soon (Peak Window)' : stage === 3 ? 'Firm Ripe (Slicing Ready)' : 'Active Shelf Life'}
                      </span>
                    </div>

                    {/* 5-Stage Mini Progress Gauge */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '4px', width: '120px', height: '6px' }}>
                        {[1, 2, 3, 4, 5].map((s) => (
                          <div
                            key={s}
                            style={{
                              borderRadius: '9999px',
                              backgroundColor: s <= stage ? STAGE_COLORS[s] : 'var(--border-light)',
                            }}
                          />
                        ))}
                      </div>
                      <span style={{ fontSize: '0.75rem', fontWeight: 600, color: accentColor }}>
                        {isTerminal ? 'Senescent (Overripe)' : `~${ambientRul.toFixed(1)} Days Usable`}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Right: Action Buttons */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    onClick={() => handleViewAnalysis(item)}
                    className="btn-secondary"
                    style={{ padding: '0.55rem 1rem', fontSize: '0.85rem' }}
                  >
                    <Eye size={15} />
                    <span>View Analysis</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleCompare(item)}
                    className="btn-secondary"
                    style={{ padding: '0.55rem 1rem', fontSize: '0.85rem' }}
                  >
                    <Sliders size={15} />
                    <span>Compare Storage</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => removeFromHistory(item.id)}
                    title="Delete from local history"
                    style={{
                      padding: '0.55rem',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--border-light)',
                      color: '#DC2626',
                      backgroundColor: 'transparent',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ------------------------------------------------------------- */}
      {/* 5. HISTORICAL DEGRADATION REMEDIATOR BANNER                   */}
      {/* ------------------------------------------------------------- */}
      <div
        style={{
          marginTop: '2.5rem',
          padding: '1.5rem 1.75rem',
          borderRadius: 'var(--radius-lg)',
          backgroundColor: 'var(--bg-subtle)',
          border: '1px solid var(--border-light)',
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.25rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div
            style={{
              width: '46px',
              height: '46px',
              borderRadius: '50%',
              backgroundColor: '#FFFFFF',
              boxShadow: 'var(--shadow-sm)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary-dark)',
              flexShrink: 0,
            }}
          >
            <CheckCircle2 size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.05rem', color: 'var(--color-forest)', fontWeight: 700, marginBottom: '2px' }}>
              Dynamic Environmental Remediator
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Moving produce from 20°C ambient to 10°C crisper storage decelerates ethylene emission rates by ~68%.
            </p>
          </div>
        </div>

        <Link
          to="/what-if"
          className="btn-secondary"
          style={{ backgroundColor: '#FFFFFF', padding: '0.65rem 1.25rem', fontSize: '0.85rem' }}
        >
          <Sliders size={16} />
          <span>Open What-If Simulator</span>
        </Link>
      </div>

      {/* ------------------------------------------------------------- */}
      {/* 6. CLEAR HISTORY CONFIRMATION MODAL                            */}
      {/* ------------------------------------------------------------- */}
      {showClearConfirm && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="clear-modal-title"
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(15, 41, 30, 0.55)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '1.25rem',
            animation: 'fadeIn 0.2s ease',
          }}
          onClick={(e) => {
            if (e.target === e.currentTarget) setShowClearConfirm(false);
          }}
        >
          <div
            className="fresh-card"
            style={{
              maxWidth: '480px',
              width: '100%',
              padding: '2rem',
              borderRadius: 'var(--radius-lg)',
              boxShadow: 'var(--shadow-xl)',
              backgroundColor: '#FFFFFF',
              position: 'relative',
            }}
          >
            {/* Close button */}
            <button
              type="button"
              onClick={() => setShowClearConfirm(false)}
              aria-label="Close modal"
              style={{
                position: 'absolute',
                top: '1.25rem',
                right: '1.25rem',
                border: 'none',
                background: 'transparent',
                color: 'var(--text-muted)',
                cursor: 'pointer',
                padding: '4px',
                borderRadius: '6px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <X size={20} />
            </button>

            {/* Warning Icon and Title */}
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem', marginBottom: '1.25rem' }}>
              <div
                style={{
                  width: '46px',
                  height: '46px',
                  borderRadius: '50%',
                  backgroundColor: '#FEE2E2',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#DC2626',
                  flexShrink: 0,
                }}
              >
                <AlertTriangle size={24} />
              </div>
              <div>
                <h3
                  id="clear-modal-title"
                  style={{
                    fontSize: '1.25rem',
                    fontWeight: 700,
                    color: 'var(--color-forest)',
                    marginBottom: '0.25rem',
                  }}
                >
                  Clear All Scan History?
                </h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                  Permanently remove all {history.length} locally cached produce scans.
                </p>
              </div>
            </div>

            {/* Notice Alert */}
            <div
              style={{
                backgroundColor: '#FEF2F2',
                border: '1px solid #FECACA',
                borderRadius: 'var(--radius-md)',
                padding: '0.875rem 1rem',
                marginBottom: '1.5rem',
                fontSize: '0.825rem',
                color: '#991B1B',
                lineHeight: '1.45',
              }}
            >
              <strong>Notice:</strong> Scan history is saved strictly within your browser’s local storage (localStorage). No server-side backup exists. Once cleared, this scan inventory cannot be restored.
            </div>

            {/* Action buttons */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                onClick={() => setShowClearConfirm(false)}
                className="btn-secondary"
                style={{ padding: '0.65rem 1.25rem', fontSize: '0.875rem' }}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  clearHistory();
                  setShowClearConfirm(false);
                }}
                className="btn-primary"
                style={{
                  backgroundColor: '#DC2626',
                  borderColor: '#DC2626',
                  color: '#FFFFFF',
                  padding: '0.65rem 1.25rem',
                  fontSize: '0.875rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                }}
              >
                <Trash2 size={16} />
                <span>Yes, Clear All Scans</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

