import './Greeting.css';

import {useEffect, useState} from 'react';
import { JSLogo } from '../JSLogo/JSLogo.tsx';
import type { AnimationEvent } from 'react';

export function Greeting() {
  const [isVisible, setIsVisible] = useState<boolean>(false);
  const [isAnimating, setIsAnimating] = useState<boolean>(false);

  const handleClick = () => {
    if (isVisible) {
      setIsAnimating(true);
    } else {
      setIsVisible(true);
    }
  };

  const handleAnimationEnd = (event: AnimationEvent<HTMLDivElement>) => {
    if (event.animationName === 'fadeOut') {
      setIsVisible(false);
      setIsAnimating(false);
    }
  };

  const [content, setContent] = useState('');
  useEffect(() => {
    fetch("/plays")
        .then((res) => res.json())
        .then((data) => setContent(JSON.stringify(data, null, 2)))
        .catch((err) => console.error("Error:", err));
  }, []);

  return (
    <div className="greeting-container">
      <button onClick={handleClick} className="greeting-button">
        Click me!
      </button>

      {isVisible && (
        <div className={isAnimating ? 'greeting-content fade-out' : 'greeting-content'} onAnimationEnd={handleAnimationEnd}>
          <JSLogo />
          <div>React: m</div>
          <div>I added this line and then recompiled the server</div>
          <div>Result of <pre>/plays</pre> endpoint:</div>
          <pre>{content}</pre>
        </div>
      )}
    </div>
  );
}