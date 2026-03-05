import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

/**
 * Use for img [src] when the URL can be a data: (base64) URL.
 * Angular may block data URLs; this pipe marks them as safe.
 */
@Pipe({ name: 'safeUrl', standalone: true })
export class SafeUrlPipe implements PipeTransform {
  constructor(private readonly sanitizer: DomSanitizer) {}

  transform(url: string | null | undefined): SafeUrl | null {
    const value = url?.trim();
    if (!value) return null;
    return this.sanitizer.bypassSecurityTrustUrl(value);
  }
}
