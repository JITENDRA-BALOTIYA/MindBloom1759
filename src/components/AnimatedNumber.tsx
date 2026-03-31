import { animate } from 'framer-motion';
import { useEffect, useRef, useState } from 'react';

interface AnimatedNumberProps {
  value: number;
  duration?: number;
  className?: string;
}

/**
 * Counts up smoothly when `value` changes (dashboard stats).
 */
export function AnimatedNumber({ value, duration = 0.65, className }: AnimatedNumberProps) {
  const [display, setDisplay] = useState(0);
  const fromRef = useRef(0);

  useEffect(() => {
    const controls = animate(fromRef.current, value, {
      duration,
      ease: [0.22, 1, 0.36, 1],
      onUpdate: (latest) => setDisplay(Math.round(latest)),
      onComplete: () => {
        fromRef.current = value;
      },
    });
    return () => controls.stop();
  }, [value, duration]);

  return <span className={className}>{display}</span>;
}
