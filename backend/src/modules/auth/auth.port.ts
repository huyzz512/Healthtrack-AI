export interface Challenge {
  id: string;
  userId: string;
  channel: 'EMAIL' | 'SMS';
  purpose: 'REGISTRATION' | 'PASSWORD_RESET';
  codeHash: string;
  failedAttempts: number;
  resendCount: number;
  expiresAt: Date;
  consumedAt: Date | null;
  invalidatedAt: Date | null;
  createdAt: Date;
}
export interface Refresh {
  id: string;
  userId: string;
  tokenHash: string;
  familyId: string;
  parentTokenId: string | null;
  expiresAt: Date;
  revokedAt: Date | null;
  createdAt: Date;
}
export interface AuthRecords {
  challenge(id: string): Promise<Challenge | null>;
  latest(userId: string, purpose: Challenge['purpose']): Promise<Challenge | null>;
  createChallenge(value: Challenge): Promise<void>;
  updateChallenge(id: string, values: Partial<Omit<Challenge, 'id' | 'userId'>>): Promise<void>;
  invalidateChallenges(userId: string, purpose: Challenge['purpose'], now: Date): Promise<void>;
  refresh(hash: string): Promise<Refresh | null>;
  createRefresh(value: Refresh): Promise<void>;
  revokeToken(id: string, now: Date): Promise<void>;
  revokeFamily(userId: string, familyId: string, now: Date): Promise<void>;
  revokeAll(userId: string, now: Date): Promise<void>;
  liveFamily(userId: string, familyId: string, now: Date): Promise<boolean>;
}
export abstract class AuthRepository {
  abstract in(scope: object): AuthRecords;
}
export abstract class AuthClock { abstract now(): Date }
