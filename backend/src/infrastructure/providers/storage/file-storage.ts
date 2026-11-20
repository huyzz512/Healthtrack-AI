export interface PutPrivateObjectRequest {
  readonly key: string;
  readonly body: Uint8Array;
  readonly contentType: string;
}

export interface PutPrivateObjectResult {
  readonly etag?: string;
}

export interface PrivateObject {
  readonly body: Uint8Array;
  readonly contentType?: string;
}

export interface FileStorage {
  putPrivateObject(
    request: PutPrivateObjectRequest,
  ): Promise<PutPrivateObjectResult>;
  getPrivateObject(key: string): Promise<PrivateObject>;
  deletePrivateObject(key: string): Promise<void>;
  isAvailable(): Promise<boolean>;
}
