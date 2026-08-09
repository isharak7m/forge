import { CSSProperties } from 'react';

interface SkeletonProps {
  width?: string | number;
  height?: string | number;
  borderRadius?: string | number;
  style?: CSSProperties;
  variant?: 'text' | 'card' | 'circle';
}

export function Skeleton({ width, height, borderRadius, style, variant = 'text' }: SkeletonProps) {
  const baseStyle: CSSProperties = {
    background:
      'linear-gradient(90deg, var(--bg-elevated) 25%, var(--border) 50%, var(--bg-elevated) 75%)',
    backgroundSize: '200% 100%',
    animation: 'shimmer 1.5s ease-in-out infinite',
    borderRadius:
      borderRadius ?? (variant === 'circle' ? '50%' : variant === 'card' ? '12px' : '6px'),
    width: width ?? (variant === 'circle' ? '40px' : '100%'),
    height: height ?? (variant === 'text' ? '14px' : variant === 'circle' ? '40px' : '100px'),
    ...style,
  };

  return <div style={baseStyle} />;
}

export function CardSkeleton({ height = 200 }: { height?: number }) {
  return (
    <div
      style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border)',
        borderRadius: '14px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
      }}
    >
      <Skeleton width="120px" height="16px" />
      <Skeleton width="100%" height={height - 80} borderRadius="8px" />
      <Skeleton width="80px" height="12px" />
    </div>
  );
}

export function StatSkeleton() {
  return (
    <div
      style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border)',
        borderRadius: '12px',
        padding: '16px 18px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
      }}
    >
      <Skeleton width="40px" height="40px" borderRadius="10px" />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '6px' }}>
        <Skeleton width="60px" height="11px" />
        <Skeleton width="100px" height="20px" />
      </div>
    </div>
  );
}
