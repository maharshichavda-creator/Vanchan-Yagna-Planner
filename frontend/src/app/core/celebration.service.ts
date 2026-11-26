import { DOCUMENT } from '@angular/common';
import { Injectable, inject } from '@angular/core';

const PETAL_COLORS = ['#ff8fab', '#ff5d8f', '#ffb3c6', '#ffd166', '#ff9f1c', '#f94144', '#ffc2d1'];

/** Short celebratory effects shown over the whole page. */
@Injectable({ providedIn: 'root' })
export class CelebrationService {
  private readonly doc = inject(DOCUMENT);

  /** Flower petals drifting down the screen; everything is gone after `durationMs`. */
  petalShower(durationMs = 3000, petals = 70): void {
    const view = this.doc.defaultView;
    if (!view) {
      return;
    }
    const layer = this.doc.createElement('div');
    layer.setAttribute('aria-hidden', 'true');
    layer.style.cssText = 'position:fixed;inset:0;overflow:hidden;pointer-events:none;z-index:2000;';

    const width = view.innerWidth;
    const height = view.innerHeight;
    for (let i = 0; i < petals; i++) {
      const size = 12 + Math.random() * 14;
      const petal = this.doc.createElement('div');
      petal.style.cssText =
        `position:absolute;top:0;left:0;width:${size}px;height:${size * 1.4}px;` +
        `background:${PETAL_COLORS[i % PETAL_COLORS.length]};border-radius:100% 0 100% 0;opacity:.9;`;
      layer.appendChild(petal);

      // All petals start within the first 0.6 s and fall for at most 2.4 s, so the shower ends by durationMs.
      const delay = Math.random() * durationMs * 0.2;
      const fall = durationMs * 0.6 + Math.random() * durationMs * 0.2;
      const x = Math.random() * width;
      const sway = (Math.random() - 0.5) * 160;
      const spin = (Math.random() < 0.5 ? -1 : 1) * (240 + Math.random() * 480);
      petal.animate(
        [
          { transform: `translate3d(${x}px,-40px,0) rotate(0deg)`, opacity: 1 },
          { transform: `translate3d(${x + sway}px,${height * 0.5}px,0) rotate(${spin / 2}deg)`, opacity: 1, offset: 0.5 },
          { transform: `translate3d(${x - sway / 2}px,${height + 40}px,0) rotate(${spin}deg)`, opacity: 0.8 }
        ],
        { duration: fall, delay, easing: 'ease-in', fill: 'both' }
      );
    }

    this.doc.body.appendChild(layer);
    setTimeout(() => layer.remove(), durationMs);
  }
}
