import { useRef, useEffect, useCallback, RefObject } from 'react';

interface ParallaxOptions {
  intensity?: number;
  reverse?: boolean;
}

export function useMouseParallax<T extends HTMLElement>(
  ref: RefObject<T | null>,
  options: ParallaxOptions = {}
) {
  const { intensity = 8, reverse = false } = options;
  const frameRef = useRef<number>(0);

  const handleMouse = useCallback((e: MouseEvent) => {
    if (!ref.current) return;
    const rect = ref.current.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const factor = reverse ? -1 : 1;
    const rotateX = ((e.clientY - centerY) / (rect.height / 2)) * intensity * factor;
    const rotateY = ((e.clientX - centerX) / (rect.width / 2)) * intensity * factor;

    cancelAnimationFrame(frameRef.current);
    frameRef.current = requestAnimationFrame(() => {
      if (ref.current) {
        ref.current.style.transform = `perspective(600px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
      }
    });
  }, [ref, intensity, reverse]);

  const handleLeave = useCallback(() => {
    cancelAnimationFrame(frameRef.current);
    frameRef.current = requestAnimationFrame(() => {
      if (ref.current) {
        ref.current.style.transform = 'perspective(600px) rotateX(0deg) rotateY(0deg) translateZ(0)';
      }
    });
  }, [ref]);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    el.addEventListener('mousemove', handleMouse);
    el.addEventListener('mouseleave', handleLeave);
    return () => {
      el.removeEventListener('mousemove', handleMouse);
      el.removeEventListener('mouseleave', handleLeave);
      cancelAnimationFrame(frameRef.current);
    };
  }, [ref, handleMouse, handleLeave]);
}
