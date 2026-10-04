import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  UploadCloud,
  AlertCircle,
  Loader2,
  Sparkles,
  RefreshCw,
  Trash2,
  ArrowRight,
  Info,
  CheckCircle2,
  FileImage
} from 'lucide-react';
import { predictProduce, ApiError } from '../services/api';
import { useScan } from '../context/ScanContext';

export const ScanPage: React.FC = () => {
  const navigate = useNavigate();
  const { setCurrentScan } = useScan();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [fileDimensions, setFileDimensions] = useState<{ width: number; height: number } | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [storageCondition, setStorageCondition] = useState<string>('ambient');
  const [errorMessage, setErrorMessage] = useState<{ title: string; detail: string } | null>(null);

  const handleFile = (file: File) => {
    setErrorMessage(null);

    // Validate file type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    const validExtensions = ['.jpg', '.jpeg', '.png', '.webp'];
    const hasValidExt = validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));

    if (!validTypes.includes(file.type.toLowerCase()) && !hasValidExt) {
      setErrorMessage({
        title: 'Unsupported File Format',
        detail: 'Please upload a valid image file. Supported formats: JPEG, PNG, or WEBP.',
      });
      return;
    }

    // Validate file size (10 MB max)
    const MAX_SIZE_BYTES = 10 * 1024 * 1024;
    if (file.size > MAX_SIZE_BYTES) {
      const sizeMB = (file.size / (1024 * 1024)).toFixed(1);
      setErrorMessage({
        title: 'File Too Large',
        detail: `Selected file is ${sizeMB} MB. Maximum allowed image payload is 10.0 MB.`,
      });
      return;
    }

    // Validate non-empty
    if (file.size === 0) {
      setErrorMessage({
        title: 'Empty File',
        detail: 'The selected file is empty (0 bytes). Please choose a valid photograph.',
      });
      return;
    }

    setSelectedFile(file);
    const objectUrl = URL.createObjectURL(file);
    setPreviewUrl(objectUrl);

    // Compute dimensions
    const img = new Image();
    img.onload = () => {
      setFileDimensions({ width: img.naturalWidth, height: img.naturalHeight });
    };
    img.src = objectUrl;
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleRemove = () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    setSelectedFile(null);
    setPreviewUrl(null);
    setFileDimensions(null);
    setErrorMessage(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleSubmit = async () => {
    if (!selectedFile || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      // Send multipart/form-data to POST /api/predict
      const result = await predictProduce(selectedFile, storageCondition);
      setCurrentScan(result, previewUrl || '', storageCondition);
      navigate('/analysis');
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (err.status === 400) {
          setErrorMessage({
            title: 'Invalid or Corrupted Image',
            detail: err.detail || 'The server could not read this image file. Ensure it is not corrupted.',
          });
        } else if (err.status === 413) {
          setErrorMessage({
            title: 'Payload Too Large',
            detail: 'Image exceeds the maximum allowed server upload size (10 MB).',
          });
        } else if (err.status === 0 || err.status === 503) {
          setErrorMessage({
            title: 'Backend Service Unavailable',
            detail: 'Unable to communicate with the FreshIQ ML API. Verify that the FastAPI backend server is running on port 8000.',
          });
        } else {
          setErrorMessage({
            title: 'Server Prediction Error',
            detail: err.detail || err.message || 'An unexpected error occurred during produce analysis.',
          });
        }
      } else {
        setErrorMessage({
          title: 'Network Connection Error',
          detail: 'Failed to connect to the backend server. Please check your network connection and verify backend status.',
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // Quick sample generator: creates an authentic Hass avocado texture canvas
  const handleLoadSample = (stage: number, stageName: string) => {
    setErrorMessage(null);
    const canvas = document.createElement('canvas');
    canvas.width = 300;
    canvas.height = 300;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Background gradient for avocado skin tone corresponding to stage
    const skinGradients: Record<number, [string, string, string]> = {
      1: ['#166534', '#15803D', '#14532D'], // Stage 1: Bright pebbled green
      2: ['#3F6212', '#4D7C0F', '#1A3826'], // Stage 2: Breaking olive tone
      3: ['#422006', '#2A1810', '#1C1917'], // Stage 3: Darkening firm ripe
      4: ['#1C1917', '#171412', '#0C0A09'], // Stage 4: Dark purplish-black
      5: ['#0C0A09', '#080706', '#030202'], // Stage 5: Deep dark overripe
    };

    const [c1, c2, c3] = skinGradients[stage] || skinGradients[1];
    const grad = ctx.createRadialGradient(150, 150, 20, 150, 150, 140);
    grad.addColorStop(0, c1);
    grad.addColorStop(0.7, c2);
    grad.addColorStop(1, c3);
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 300, 300);

    // Add pebbled botanical skin texture dots
    for (let i = 0; i < 400; i++) {
      const x = Math.random() * 300;
      const y = Math.random() * 300;
      const radius = Math.random() * 2.5 + 0.8;
      ctx.fillStyle = Math.random() > 0.5 ? 'rgba(255, 255, 255, 0.09)' : 'rgba(0, 0, 0, 0.2)';
      ctx.beginPath();
      ctx.arc(x, y, radius, 0, Math.PI * 2);
      ctx.fill();
    }

    canvas.toBlob((blob) => {
      if (blob) {
        const sampleFile = new File([blob], `sample_avocado_${stageName.toLowerCase().replace(/\s+/g, '_')}.jpg`, {
          type: 'image/jpeg',
        });
        handleFile(sampleFile);
      }
    }, 'image/jpeg', 0.95);
  };

  return (
    <div className="container" style={{ maxWidth: '1280px', paddingBottom: '3rem' }}>
      {/* 1. TOP CONTEXTUAL HEADER & STEPPER */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'flex-end',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.5rem',
          marginBottom: '2.5rem',
        }}
      >
        <div style={{ maxWidth: '640px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '4px 12px',
                borderRadius: '9999px',
                backgroundColor: 'var(--color-primary-light)',
                color: 'var(--color-primary-dark)',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.725rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.04em',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-primary)' }} />
              Step 1 of 3: Produce Intake
            </span>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Batch Pipeline: #FIQ-OPTICAL
            </span>
          </div>

          <h1 style={{ fontSize: 'clamp(1.85rem, 3.5vw, 2.5rem)', color: 'var(--color-forest)', marginBottom: '0.5rem' }}>
            Scan &amp; Estimate Ripeness
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', lineHeight: '1.5' }}>
            Upload a clear photo of your Hass avocado to determine optical ripening stage, shelf-life envelope, and remaining usable days.
          </p>
        </div>

        {/* Stepper Pills */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: '#FFFFFF',
            border: '1px solid var(--border-light)',
            padding: '8px 16px',
            borderRadius: '9999px',
            boxShadow: 'var(--shadow-sm)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--color-forest)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 700 }}>
              1
            </span>
            <span style={{ fontSize: '0.825rem', fontWeight: 700, color: 'var(--color-forest)' }}>Intake</span>
          </div>
          <span style={{ color: 'var(--text-subtle)', fontSize: '0.8rem' }}>&rarr;</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', opacity: 0.5 }}>
            <span style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', color: 'var(--text-main)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 600 }}>
              2
            </span>
            <span style={{ fontSize: '0.825rem', fontWeight: 500, color: 'var(--text-muted)' }}>Vision AI</span>
          </div>
          <span style={{ color: 'var(--text-subtle)', fontSize: '0.8rem' }}>&rarr;</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', opacity: 0.5 }}>
            <span style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--bg-subtle)', color: 'var(--text-main)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 600 }}>
              3
            </span>
            <span style={{ fontSize: '0.825rem', fontWeight: 500, color: 'var(--text-muted)' }}>What-If RUL</span>
          </div>
        </div>
      </div>

      {/* ERROR BANNER WITH RETRY OPTION */}
      {errorMessage && (
        <div
          style={{
            backgroundColor: '#FEF2F2',
            border: '1px solid #FCA5A5',
            borderRadius: 'var(--radius-md)',
            padding: '1.25rem',
            marginBottom: '2rem',
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '12px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', flex: 1, minWidth: '240px' }}>
            <AlertCircle size={22} style={{ color: '#DC2626', flexShrink: 0, marginTop: '2px' }} />
            <div>
              <h4 style={{ color: '#991B1B', fontWeight: 700, fontSize: '0.95rem', marginBottom: '2px' }}>
                {errorMessage.title}
              </h4>
              <p style={{ color: '#B91C1C', fontSize: '0.875rem', lineHeight: '1.5', margin: 0 }}>
                {errorMessage.detail}
              </p>
            </div>
          </div>
          {selectedFile && (
            <button
              type="button"
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="btn-primary"
              style={{
                backgroundColor: '#DC2626',
                padding: '0.5rem 1rem',
                fontSize: '0.85rem',
                borderRadius: '9999px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                flexShrink: 0,
              }}
            >
              <RefreshCw size={14} className={isSubmitting ? 'animate-spin' : ''} />
              <span>Retry Analysis</span>
            </button>
          )}
        </div>
      )}

      {/* 2. MAIN TWO-COLUMN LAYOUT */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: '2rem',
          alignItems: 'start',
        }}
      >
        {/* LEFT COLUMN: DROPZONE & STAGED IMAGE (7 cols equivalent) */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <input
            type="file"
            ref={fileInputRef}
            accept="image/jpeg,image/png,image/webp"
            style={{ display: 'none' }}
            onChange={(e) => {
              if (e.target.files && e.target.files[0]) {
                handleFile(e.target.files[0]);
              }
            }}
          />

          {!previewUrl ? (
            /* STATE 1: EMPTY DROPZONE */
            <div className="fresh-card" style={{ padding: '2rem' }}>
              <div
                tabIndex={0}
                role="button"
                aria-label="Upload produce photo dropzone. Click or press Enter to browse files."
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    fileInputRef.current?.click();
                  }
                }}
                onDragOver={(e) => {
                  e.preventDefault();
                  setIsDragging(true);
                }}
                onDragLeave={() => setIsDragging(false)}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
                style={{
                  border: `2px dashed ${isDragging ? 'var(--color-primary)' : '#CBD5E1'}`,
                  borderRadius: 'var(--radius-lg)',
                  backgroundColor: isDragging ? 'var(--color-primary-light)' : 'rgba(241, 245, 249, 0.65)',
                  padding: '3.5rem 1.5rem',
                  cursor: 'pointer',
                  textAlign: 'center',
                  transition: 'all 0.2s ease',
                }}
              >
                {/* Upload glyph with ping */}
                <div
                  style={{
                    position: 'relative',
                    width: '64px',
                    height: '64px',
                    borderRadius: '50%',
                    backgroundColor: '#FFFFFF',
                    color: 'var(--color-forest)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 1.25rem',
                    boxShadow: 'var(--shadow-md)',
                  }}
                >
                  <UploadCloud size={30} style={{ color: 'var(--color-primary)' }} />
                  <span style={{ position: 'absolute', top: '-2px', right: '-2px', display: 'flex', height: '14px', width: '14px' }}>
                    <span style={{ position: 'absolute', height: '100%', width: '100%', borderRadius: '50%', backgroundColor: 'var(--color-primary)', opacity: 0.75 }} className="animate-pulse-subtle" />
                    <span style={{ position: 'relative', borderRadius: '50%', height: '14px', width: '14px', backgroundColor: 'var(--color-primary)' }} />
                  </span>
                </div>

                <h3 style={{ fontSize: '1.25rem', color: 'var(--color-forest)', marginBottom: '0.35rem' }}>
                  Drag and drop your produce photo here
                </h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', maxWidth: '380px', margin: '0 auto 1.5rem', lineHeight: '1.5' }}>
                  Upload high-resolution single produce captures for multi-band skin texture calibration.
                </p>

                <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '0.75rem', marginBottom: '1.25rem' }}>
                  <button
                    type="button"
                    className="btn-primary"
                    style={{ borderRadius: '9999px', padding: '0.75rem 1.5rem' }}
                    onClick={(e) => {
                      e.stopPropagation();
                      fileInputRef.current?.click();
                    }}
                  >
                    <FileImage size={17} />
                    <span>Choose Image File</span>
                  </button>
                  <button
                    type="button"
                    disabled={true}
                    className="btn-secondary"
                    style={{
                      borderRadius: '9999px',
                      padding: '0.75rem 1.5rem',
                      opacity: 0.55,
                      cursor: 'not-allowed',
                      backgroundColor: 'var(--bg-subtle)',
                      borderColor: 'var(--border-light)',
                      color: 'var(--text-muted)',
                    }}
                    title="Upload or select an avocado image first"
                  >
                    <Sparkles size={17} />
                    <span>Analyze Produce (Select Image First)</span>
                  </button>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  <span style={{ padding: '2px 6px', backgroundColor: '#E2E8F0', borderRadius: '4px' }}>JPG</span>
                  <span style={{ padding: '2px 6px', backgroundColor: '#E2E8F0', borderRadius: '4px' }}>PNG</span>
                  <span style={{ padding: '2px 6px', backgroundColor: '#E2E8F0', borderRadius: '4px' }}>WEBP</span>
                  <span>&bull; Max 10 MB payload</span>
                </div>
              </div>

              {/* Quick Sample Selector in Dropzone */}
              <div style={{ marginTop: '1.75rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border-light)' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', marginBottom: '0.85rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  <Sparkles size={16} style={{ color: 'var(--color-primary)' }} />
                  <span>Or test instantly with a synthetic calibrated specimen:</span>
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '0.5rem' }}>
                  {[
                    { stage: 1, name: 'Stage 1 (Underripe)' },
                    { stage: 2, name: 'Stage 2 (Breaking)' },
                    { stage: 3, name: 'Stage 3 (Firm Ripe)' },
                    { stage: 4, name: 'Stage 4 (Peak Ripe)' },
                    { stage: 5, name: 'Stage 5 (Overripe)' },
                  ].map((s) => (
                    <button
                      key={s.stage}
                      type="button"
                      onClick={() => handleLoadSample(s.stage, s.name)}
                      style={{
                        backgroundColor: 'var(--bg-subtle)',
                        border: '1px solid var(--border-light)',
                        borderRadius: '9999px',
                        padding: '6px 12px',
                        fontSize: '0.775rem',
                        fontWeight: 600,
                        color: 'var(--color-forest)',
                        transition: 'all 0.15s ease',
                      }}
                      className="sample-btn"
                    >
                      {s.name}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            /* STATE 2 & 3: STAGED IMAGE READY FOR INFERENCE */
            <div className="fresh-card" style={{ padding: '1.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <CheckCircle2 size={20} style={{ color: 'var(--color-primary)' }} />
                  <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', margin: 0 }}>
                    Staged Produce Specimen
                  </h3>
                </div>
                <span
                  style={{
                    backgroundColor: isSubmitting ? '#FEF3C7' : 'var(--color-primary-light)',
                    color: isSubmitting ? '#92400E' : 'var(--color-primary-dark)',
                    padding: '3px 10px',
                    borderRadius: '9999px',
                    fontFamily: 'var(--font-mono)',
                    fontSize: '0.725rem',
                    fontWeight: 700,
                  }}
                >
                  {isSubmitting ? 'ANALYZING TENSORS...' : 'READY FOR INFERENCE'}
                </span>
              </div>

              {/* Optical Bounding Frame Simulation Overlay */}
              <div
                style={{
                  position: 'relative',
                  width: '100%',
                  borderRadius: 'var(--radius-md)',
                  overflow: 'hidden',
                  backgroundColor: '#0F172A',
                  boxShadow: 'var(--shadow-md)',
                  marginBottom: '1.25rem',
                }}
              >
                <img
                  src={previewUrl}
                  alt="Staged produce"
                  style={{ width: '100%', height: '360px', objectFit: 'cover', display: 'block', opacity: isSubmitting ? 0.7 : 1, transition: 'opacity 0.3s ease' }}
                />

                {/* Reticle Overlay */}
                <div
                  style={{
                    position: 'absolute',
                    inset: '16px',
                    border: '1px dashed rgba(255, 255, 255, 0.45)',
                    borderRadius: '12px',
                    pointerEvents: 'none',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ backgroundColor: 'rgba(255, 255, 255, 0.92)', backdropFilter: 'blur(8px)', padding: '4px 10px', borderRadius: '6px', fontSize: '0.7rem', fontFamily: 'var(--font-mono)', color: 'var(--color-forest)', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-primary)' }} className="animate-pulse-subtle" />
                      ROI 1: Epicarp Surface
                    </div>
                    <div style={{ backgroundColor: 'rgba(15, 41, 30, 0.85)', color: '#FFFFFF', padding: '4px 10px', borderRadius: '6px', fontSize: '0.7rem', fontFamily: 'var(--font-mono)' }}>
                      Optical Calibration
                    </div>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ backgroundColor: 'rgba(255, 255, 255, 0.92)', padding: '4px 10px', borderRadius: '6px', fontSize: '0.7rem', fontFamily: 'var(--font-mono)', color: 'var(--text-main)' }}>
                      Format: {selectedFile?.type.replace('image/', '').toUpperCase()}
                    </div>
                    <div style={{ backgroundColor: 'rgba(255, 255, 255, 0.92)', padding: '4px 10px', borderRadius: '6px', fontSize: '0.7rem', fontFamily: 'var(--font-mono)', color: 'var(--color-primary)', fontWeight: 700 }}>
                      Coverage: Centered
                    </div>
                  </div>
                </div>

                {/* Submitting Loading Overlay */}
                {isSubmitting && (
                  <div
                    style={{
                      position: 'absolute',
                      inset: 0,
                      backgroundColor: 'rgba(15, 41, 30, 0.75)',
                      backdropFilter: 'blur(4px)',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#FFFFFF',
                      gap: '12px',
                    }}
                  >
                    <Loader2 size={36} className="animate-spin" style={{ color: 'var(--color-lime-glow)' }} />
                    <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>Running Produce AI Inference</div>
                    <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: '#CBD5E1' }}>
                      MobileNetV3-Small &bull; HistGradientBoosting
                    </div>
                  </div>
                )}
              </div>

              {/* Metadata strip & Retake/Remove Actions */}
              <div
                style={{
                  backgroundColor: 'var(--bg-subtle)',
                  borderRadius: 'var(--radius-md)',
                  padding: '12px 16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '12px',
                  marginBottom: '1.5rem',
                }}
              >
                <div>
                  <div style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--color-forest)' }}>
                    {selectedFile?.name}
                  </div>
                  <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    {((selectedFile?.size || 0) / (1024 * 1024)).toFixed(2)} MB
                    {fileDimensions ? ` • ${fileDimensions.width} × ${fileDimensions.height} px` : ''}
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isSubmitting}
                    className="btn-secondary"
                    style={{ padding: '6px 12px', borderRadius: '9999px', fontSize: '0.8rem' }}
                  >
                    <RefreshCw size={14} />
                    <span>Retake</span>
                  </button>
                  <button
                    type="button"
                    onClick={handleRemove}
                    disabled={isSubmitting}
                    className="btn-secondary"
                    style={{ padding: '6px 12px', borderRadius: '9999px', fontSize: '0.8rem', color: '#DC2626' }}
                  >
                    <Trash2 size={14} />
                    <span>Remove</span>
                  </button>
                </div>
              </div>

              {/* Optional Storage Condition Hint Selector */}
              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', fontSize: '0.825rem', fontWeight: 700, color: 'var(--color-forest)', textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: '6px' }}>
                  Current Storage Environment:
                </label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(135px, 1fr))', gap: '8px' }}>
                  {[
                    { id: 'ambient', label: 'Ambient Counter (~20°C)' },
                    { id: '20C', label: 'Controlled Room (20°C)' },
                    { id: '10C', label: 'Cold Storage (10°C)' },
                    { id: '4C', label: 'Refrigerator (4°C)' },
                  ].map((cond) => (
                    <button
                      key={cond.id}
                      type="button"
                      disabled={isSubmitting}
                      onClick={() => setStorageCondition(cond.id)}
                      style={{
                        padding: '8px 10px',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.8rem',
                        fontWeight: storageCondition === cond.id ? 700 : 500,
                        border: `1px solid ${storageCondition === cond.id ? 'var(--color-primary)' : 'var(--border-light)'}`,
                        backgroundColor: storageCondition === cond.id ? 'var(--color-primary-light)' : '#FFFFFF',
                        color: storageCondition === cond.id ? 'var(--color-primary-dark)' : 'var(--text-muted)',
                        transition: 'all 0.15s ease',
                      }}
                    >
                      {cond.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Main Submit CTA */}
              <button
                type="button"
                onClick={handleSubmit}
                disabled={isSubmitting}
                className="btn-primary"
                style={{
                  width: '100%',
                  padding: '1rem',
                  fontSize: '1.05rem',
                  borderRadius: '9999px',
                  backgroundColor: 'var(--color-forest)',
                  boxShadow: '0 4px 14px rgba(15, 41, 30, 0.25)',
                  cursor: isSubmitting ? 'not-allowed' : 'pointer',
                }}
              >
                {isSubmitting ? (
                  <>
                    <Loader2 size={20} className="animate-spin" />
                    <span>Extracting Ripening Tensors...</span>
                  </>
                ) : (
                  <>
                    <Sparkles size={20} style={{ color: 'var(--color-lime-glow)' }} />
                    <span>Analyze Freshness &amp; Shelf-Life</span>
                    <ArrowRight size={18} />
                  </>
                )}
              </button>
              <p style={{ textAlign: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', color: 'var(--text-muted)', marginTop: '8px' }}>
                Estimated inference latency: ~25ms on MobileNetV3 + HistGradientBoosting
              </p>
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: MODEL SCOPE & QUALITY GUIDELINES (5 cols equivalent) */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Active Model Scope Card */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Produce Taxonomy
              </span>
              <span
                style={{
                  backgroundColor: 'var(--color-primary-light)',
                  color: 'var(--color-primary-dark)',
                  padding: '3px 10px',
                  borderRadius: '9999px',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.725rem',
                  fontWeight: 700,
                }}
              >
                ACTIVE PRODUCTION
              </span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)', padding: '1rem', marginBottom: '1rem' }}>
              <div style={{ width: '50px', height: '50px', borderRadius: '12px', overflow: 'hidden', flexShrink: 0 }}>
                <img
                  src="https://lh3.googleusercontent.com/aida/AEtjO1UdxPkw8rk1Ql1rraKENzW72qNbFaIlJerCJJOcer5xvrH5We1rJ_buCEHjJos3JgsFMkWg9BDDnswbVARCFnTbbC1j4Tl_0Y3VmUzt1XqRZD4fpaDcsyvou7zLr-Rf_UMhWAsTHMned0jO-Asp1DJgy_cY00NhlVTAh_NdEZY-EBUnwQ9MIj0YwvoXR6len3bzKsD10zavNqTa9Si2G2CDy7A7Wb5bGSSjtfAsxLF4Dt_uVGERkCDAbPI"
                  alt="Hass avocado botanical specimen"
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              </div>
              <div>
                <h4 style={{ fontSize: '1.05rem', color: 'var(--color-forest)', margin: 0, fontWeight: 700 }}>Hass Avocado</h4>
                <p style={{ fontFamily: 'var(--font-mono)', fontSize: '0.775rem', color: 'var(--text-muted)', fontStyle: 'italic', margin: '2px 0 4px' }}>
                  Persea americana cv. Hass
                </p>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                  Calibrated for climacteric respiration kinetics, skin melanin pigmentation shifts, and firmness decay.
                </p>
              </div>
            </div>

            <div style={{ backgroundColor: '#F0FDF4', border: '1px solid #DCFCE7', borderRadius: 'var(--radius-sm)', padding: '10px 12px', display: 'flex', gap: '8px', fontSize: '0.8rem', color: 'var(--color-primary-dark)' }}>
              <Info size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
              <span>
                <strong>Single-Produce Calibration:</strong> FreshIQ's neural weights are currently calibrated exclusively for Hass avocados.
              </span>
            </div>
          </div>

          {/* Image Quality Guidelines Card (4 Concise Rules) */}
          <div className="fresh-card" style={{ padding: '1.75rem' }}>
            <div style={{ marginBottom: '1.25rem' }}>
              <h3 style={{ fontSize: '1.15rem', color: 'var(--color-forest)', marginBottom: '4px' }}>
                Image Quality Guidelines
              </h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                Follow these 4 capture rules to maximize MobileNetV3 feature extraction precision:
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {/* Rule 1 */}
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--bg-subtle)' }}>
                <div style={{ width: '24px', height: '24px', borderRadius: '50%', backgroundColor: 'var(--color-forest)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 700, flexShrink: 0, marginTop: '2px' }}>
                  1
                </div>
                <div>
                  <h5 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--color-forest)', marginBottom: '2px' }}>
                    Natural, Diffuse Lighting
                  </h5>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                    Avoid harsh camera flash bursts or pitch-black shadows that wash out epidermal chlorophyll tone.
                  </p>
                </div>
              </div>

              {/* Rule 2 */}
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--bg-subtle)' }}>
                <div style={{ width: '24px', height: '24px', borderRadius: '50%', backgroundColor: 'var(--color-forest)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 700, flexShrink: 0, marginTop: '2px' }}>
                  2
                </div>
                <div>
                  <h5 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--color-forest)', marginBottom: '2px' }}>
                    50% to 70% Frame Fill
                  </h5>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                    Position the avocado centrally so pebble density and skin texture occupy the primary field of view.
                  </p>
                </div>
              </div>

              {/* Rule 3 */}
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--bg-subtle)' }}>
                <div style={{ width: '24px', height: '24px', borderRadius: '50%', backgroundColor: 'var(--color-forest)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 700, flexShrink: 0, marginTop: '2px' }}>
                  3
                </div>
                <div>
                  <h5 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--color-forest)', marginBottom: '2px' }}>
                    Clean, Uniform Countertop
                  </h5>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                    Place produce on a cutting board, marble, or clean table. Remove busy extraneous clutter.
                  </p>
                </div>
              </div>

              {/* Rule 4 */}
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '10px', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--bg-subtle)' }}>
                <div style={{ width: '24px', height: '24px', borderRadius: '50%', backgroundColor: 'var(--color-forest)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: '0.725rem', fontWeight: 700, flexShrink: 0, marginTop: '2px' }}>
                  4
                </div>
                <div>
                  <h5 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--color-forest)', marginBottom: '2px' }}>
                    No Mesh Bags or Cling Film
                  </h5>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                    Polyethylene wrap and mesh packaging cause optical refraction artifacts on the CNN feature layers.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
