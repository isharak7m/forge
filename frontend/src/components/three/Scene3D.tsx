import { useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Box, Sphere, Torus } from '@react-three/drei';
import * as THREE from 'three';

function WireframeDumbbellInner() {
  const groupRef = useRef<THREE.Group>(null);
  useFrame((_, delta) => {
    if (groupRef.current) {
      groupRef.current.rotation.x += delta * 0.3;
      groupRef.current.rotation.y += delta * 0.5;
    }
  });
  return (
    <group ref={groupRef}>
      {/* Bar */}
      <Box args={[2, 0.08, 0.08]} position={[0, 0, 0]}>
        <meshBasicMaterial color="#4a5265" wireframe />
      </Box>
      {/* Left weight plate */}
      <Torus args={[0.35, 0.08, 8, 16]} position={[-0.8, 0, 0]} rotation={[Math.PI / 2, 0, 0]}>
        <meshBasicMaterial color="#22d3ee" wireframe />
      </Torus>
      <Sphere args={[0.18, 8, 8]} position={[-1.15, 0, 0]}>
        <meshBasicMaterial color="#22d3ee" wireframe />
      </Sphere>
      {/* Right weight plate */}
      <Torus args={[0.35, 0.08, 8, 16]} position={[0.8, 0, 0]} rotation={[Math.PI / 2, 0, 0]}>
        <meshBasicMaterial color="#a78bfa" wireframe />
      </Torus>
      <Sphere args={[0.18, 8, 8]} position={[1.15, 0, 0]}>
        <meshBasicMaterial color="#a78bfa" wireframe />
      </Sphere>
    </group>
  );
}

export function FloatingDumbbell() {
  return (
    <div style={{ width: '120px', height: '120px', position: 'relative', pointerEvents: 'none' }}>
      <Canvas camera={{ position: [0, 0, 4], fov: 50 }} style={{ background: 'transparent' }}>
        <ambientLight intensity={0.8} />
        <WireframeDumbbellInner />
      </Canvas>
    </div>
  );
}

function FloatingRingsInner() {
  const ring1 = useRef<THREE.Mesh>(null);
  const ring2 = useRef<THREE.Mesh>(null);
  useFrame((_, delta) => {
    if (ring1.current) ring1.current.rotation.y += delta * 0.4;
    if (ring1.current) ring1.current.rotation.x += delta * 0.2;
    if (ring2.current) ring2.current.rotation.y -= delta * 0.3;
    if (ring2.current) ring2.current.rotation.z += delta * 0.15;
  });
  return (
    <group>
      <Torus ref={ring1} args={[1.2, 0.03, 16, 48]} position={[0, 0, 0]} rotation={[Math.PI / 3, 0, 0]}>
        <meshBasicMaterial color="#22d3ee" opacity={0.3} transparent />
      </Torus>
      <Torus ref={ring2} args={[1.0, 0.025, 16, 48]} position={[0, 0, 0]} rotation={[-Math.PI / 4, Math.PI / 6, 0]}>
        <meshBasicMaterial color="#a78bfa" opacity={0.25} transparent />
      </Torus>
    </group>
  );
}

export function FloatingRingsScene() {
  return (
    <div style={{ width: '200px', height: '200px', position: 'relative', pointerEvents: 'none' }}>
      <Canvas camera={{ position: [0, 0, 3.5], fov: 45 }} style={{ background: 'transparent' }}>
        <FloatingRingsInner />
      </Canvas>
    </div>
  );
}

function HolographicSphereInner() {
  const meshRef = useRef<THREE.Mesh>(null);
  useFrame((_, delta) => {
    if (meshRef.current) meshRef.current.rotation.y += delta * 0.6;
    if (meshRef.current) meshRef.current.rotation.x += delta * 0.3;
  });
  return (
    <Sphere ref={meshRef} args={[1.2, 24, 24]} position={[0, 0, 0]}>
      <meshBasicMaterial color="#22d3ee" wireframe opacity={0.2} transparent />
    </Sphere>
  );
}

export function HolographicSphere() {
  return (
    <div style={{ width: '160px', height: '160px', position: 'relative', pointerEvents: 'none' }}>
      <Canvas camera={{ position: [0, 0, 3.5], fov: 45 }} style={{ background: 'transparent' }}>
        <HolographicSphereInner />
      </Canvas>
    </div>
  );
}
