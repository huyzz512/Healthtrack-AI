export interface PdfRenderRequest {
  readonly documentId: string;
  readonly title: string;
  readonly model: Readonly<Record<string, unknown>>;
}

export interface RenderedPdf {
  readonly bytes: Uint8Array;
  readonly contentType: 'application/pdf';
  readonly provider: string;
}

export interface PdfRenderer {
  render(request: PdfRenderRequest): Promise<RenderedPdf>;
}
