import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { BrowserMultiFormatReader, IScannerControls } from '@zxing/browser';
import { BarcodeFormat, DecodeHintType } from '@zxing/library';

/** Skannar EAN-13-streckkoder (ISBN) med enhetens kamera. Kräver HTTPS eller localhost. */
@Component({
  selector: 'app-isbn-scanner',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <video #video class="video" playsinline muted></video>
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
  `,
  styles: `
    .video { width: 100%; max-height: 50vh; background: #000; border-radius: 8px; object-fit: cover; }
    .error { color: #b00020; }
  `,
})
export class IsbnScanner implements OnDestroy {
  readonly detected = output<string>();
  protected readonly error = signal<string | null>(null);

  private readonly video = viewChild.required<ElementRef<HTMLVideoElement>>('video');
  private controls?: IScannerControls;
  private lastCode = '';
  private destroyed = false;

  constructor() {
    afterNextRender(() => void this.start());
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.controls?.stop();
  }

  private async start(): Promise<void> {
    const hints = new Map<DecodeHintType, unknown>([
      [DecodeHintType.POSSIBLE_FORMATS, [BarcodeFormat.EAN_13]],
    ]);
    const reader = new BrowserMultiFormatReader(hints);
    try {
      const controls = await reader.decodeFromConstraints(
        { video: { facingMode: { ideal: 'environment' } } },
        this.video().nativeElement,
        (result) => {
          const code = result?.getText();
          // ISBN-13 börjar med 978/979; ignorera andra EAN-koder och upprepade träffar.
          if (code && /^97[89]\d{10}$/.test(code) && code !== this.lastCode) {
            this.lastCode = code;
            this.detected.emit(code);
          }
        },
      );
      if (this.destroyed) {
        controls.stop();
      } else {
        this.controls = controls;
      }
    } catch {
      this.error.set('Kunde inte starta kameran. Kontrollera behörighet, eller ange ISBN manuellt.');
    }
  }
}
