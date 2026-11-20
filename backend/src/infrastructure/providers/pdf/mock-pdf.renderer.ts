import { Injectable } from '@nestjs/common';

import type {
  PdfRenderer,
  PdfRenderRequest,
  RenderedPdf,
} from './pdf-renderer.js';

const MOCK_PDF = Buffer.from(
  '%PDF-1.4\n% HealthTrack AI Foundation mock renderer\n%%EOF\n',
  'utf8',
);

@Injectable()
export class MockPdfRenderer implements PdfRenderer {
  render(request: PdfRenderRequest): Promise<RenderedPdf> {
    void request;

    return Promise.resolve({
      bytes: MOCK_PDF,
      contentType: 'application/pdf',
      provider: 'mock',
    });
  }
}
