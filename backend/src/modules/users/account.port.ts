export type AccountStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'LOCKED' | 'DISABLED';

export interface Account {
  id: string;
  fullName: string;
  email: string | null;
  phone: string | null;
  status: AccountStatus;
  emailVerifiedAt: Date | null;
  phoneVerifiedAt: Date | null;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;
  credential: {
    passwordHash: string;
    failedLoginCount: number;
    failedLoginWindowAt: Date | null;
    lockedUntil: Date | null;
  } | null;
}

export interface Identity { email?: string; phone?: string }

export interface Accounts {
  find(identity: Identity): Promise<Account | null>;
  get(id: string): Promise<Account | null>;
  create(input: Identity & { fullName: string; passwordHash: string }): Promise<Account>;
  activate(id: string, channel: 'EMAIL' | 'SMS', now: Date): Promise<void>;
  failures(id: string, count: number, window: Date | null, lockedUntil: Date | null): Promise<void>;
  password(id: string, hash: string, now: Date): Promise<void>;
  rename(id: string, fullName: string): Promise<Account>;
}

// The scope is an opaque transaction capability. Only infrastructure adapters
// inspect it; application services never receive a Prisma client type.
export abstract class AccountUnitOfWork {
  abstract read<T>(work: (accounts: Accounts, scope: object) => Promise<T>): Promise<T>;
  abstract run<T>(key: string, work: (accounts: Accounts, scope: object) => Promise<T>): Promise<T>;
}
