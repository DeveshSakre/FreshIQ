import React, { useState, useEffect } from 'react';
import { NavLink, Link } from 'react-router-dom';
import { Camera, Sparkles, Sliders, History, BookOpen, Menu, X } from 'lucide-react';
import { HealthIndicator } from '../components/HealthIndicator';

export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // Close mobile menu on Escape key press
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && mobileMenuOpen) {
        setMobileMenuOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [mobileMenuOpen]);

  // Exact navigation items specified in Stitch design
  const navLinks = [
    { to: '/', label: 'Home', icon: Sparkles },
    { to: '/scan', label: 'Scan Produce', icon: Camera },
    { to: '/history', label: 'History', icon: History },
    { to: '/what-if', label: 'What-If Lab', icon: Sliders },
    { to: '/insights', label: 'ML Insights', icon: BookOpen },
  ];

  return (
    <header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 50,
        backgroundColor: 'rgba(244, 251, 243, 0.92)',
        backdropFilter: 'blur(16px)',
        borderBottom: '1px solid rgba(194, 200, 193, 0.4)',
        boxShadow: '0 1px 8px rgba(26, 56, 38, 0.04)',
      }}
    >
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: '74px' }}>
        {/* Brand Logo & Tagline */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <img src="/logo.svg" alt="FreshIQ Logo" style={{ height: '36px', width: 'auto' }} />
          <div style={{ display: 'none', flexDirection: 'column' }} className="brand-text">
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.675rem', color: 'var(--text-muted)', fontWeight: 600, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
              Precision Shelf-Life AI
            </span>
          </div>
        </Link>

        {/* Desktop Rounded Pill Nav Bar (Stitch Design Pattern) */}
        <nav
          style={{
            display: 'none',
            alignItems: 'center',
            gap: '4px',
            padding: '4px 6px',
            backgroundColor: 'rgba(238, 246, 237, 0.85)',
            borderRadius: '9999px',
            border: '1px solid rgba(194, 200, 193, 0.4)',
          }}
          className="desktop-nav"
        >
          {navLinks.map((link) => {
            return (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.to === '/'}
                style={({ isActive }) => ({
                  display: 'inline-flex',
                  alignItems: 'center',
                  padding: '7px 16px',
                  borderRadius: '9999px',
                  fontSize: '0.875rem',
                  fontWeight: isActive ? 700 : 500,
                  color: isActive ? 'var(--color-forest)' : 'var(--text-muted)',
                  backgroundColor: isActive ? '#FFFFFF' : 'transparent',
                  boxShadow: isActive ? '0 1px 3px rgba(0,0,0,0.06)' : 'none',
                  transition: 'all 0.15s ease',
                })}
              >
                <span>{link.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Right Side Controls: Model Pill Badge & Prominent Scan Produce CTA */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {/* Health Indicator */}
          <HealthIndicator />

          {/* Prominent Scan Produce Button */}
          <Link
            to="/scan"
            className="btn-primary"
            style={{
              display: 'none',
              borderRadius: '9999px',
              padding: '0.65rem 1.35rem',
              fontSize: '0.875rem',
              backgroundColor: 'var(--color-forest)',
            }}
            id="nav-cta-btn"
          >
            <Camera size={16} />
            <span style={{ letterSpacing: '0.02em', textTransform: 'uppercase', fontSize: '0.775rem', fontWeight: 700 }}>
              Scan Produce
            </span>
          </Link>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            style={{
              padding: '8px',
              borderRadius: '8px',
              color: 'var(--text-main)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
            className="mobile-menu-toggle"
            aria-label="Toggle navigation menu"
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-navigation"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div
          style={{
            backgroundColor: '#FFFFFF',
            borderBottom: '1px solid var(--border-light)',
            padding: '16px 24px 24px',
            boxShadow: 'var(--shadow-lg)',
          }}
          className="mobile-drawer"
          id="mobile-navigation"
        >
          <nav style={{ display: 'flex', flexDirection: 'column', gap: '8px' }} aria-label="Mobile navigation">
            {navLinks.map((link) => {
              const Icon = link.icon;
              return (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === '/'}
                  onClick={() => setMobileMenuOpen(false)}
                  style={({ isActive }) => ({
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    padding: '12px 16px',
                    borderRadius: '12px',
                    fontSize: '0.95rem',
                    fontWeight: isActive ? 600 : 500,
                    color: isActive ? 'var(--color-forest)' : 'var(--text-main)',
                    backgroundColor: isActive ? 'var(--color-primary-light)' : 'var(--bg-subtle)',
                  })}
                >
                  <Icon size={18} />
                  <span>{link.label}</span>
                </NavLink>
              );
            })}
            <Link
              to="/scan"
              onClick={() => setMobileMenuOpen(false)}
              className="btn-accent"
              style={{ marginTop: '10px', justifyContent: 'center' }}
            >
              <Camera size={18} />
              <span>Scan Produce</span>
            </Link>
          </nav>
        </div>
      )}

      <style>{`
        @media (min-width: 960px) {
          .desktop-nav {
            display: flex !important;
          }
          .brand-text {
            display: flex !important;
          }
          #nav-cta-btn {
            display: inline-flex !important;
          }
          .mobile-menu-toggle {
            display: none !important;
          }
          .mobile-drawer {
            display: none !important;
          }
        }
      `}</style>
    </header>
  );
};
