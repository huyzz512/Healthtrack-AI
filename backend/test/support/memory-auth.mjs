import { randomUUID } from 'node:crypto';

// Transactional test double. Real PostgreSQL races are checked by the separate
// integration suite; this double is not evidence of database lock behavior.
export function memoryAuth(clock) {
  let state = { accounts: new Map(), challenges: new Map(), tokens: new Map() };
  let chain = Promise.resolve();
  const copy = (value) => value ? structuredClone(value) : null;
  const accounts = {
    async find(identity) { return copy([...state.accounts.values()].find(a => identity.email ? a.email === identity.email : a.phone === identity.phone)); },
    async get(id) { return copy(state.accounts.get(id)); },
    async create(input) {
      const now = clock.now();
      const a = { id: randomUUID(), email: input.email ?? null, phone: input.phone ?? null, fullName: input.fullName, status: 'PENDING_VERIFICATION', emailVerifiedAt: null, phoneVerifiedAt: null, createdAt: now, updatedAt: now, deletedAt: null, credential: { passwordHash: input.passwordHash, failedLoginCount: 0, failedLoginWindowAt: null, lockedUntil: null } };
      state.accounts.set(a.id, a); return copy(a);
    },
    async activate(id, channel, now) { const a = state.accounts.get(id); a.status = 'ACTIVE'; a[channel === 'EMAIL' ? 'emailVerifiedAt' : 'phoneVerifiedAt'] = now; },
    async failures(id, count, window, lockedUntil) { Object.assign(state.accounts.get(id).credential, { failedLoginCount: count, failedLoginWindowAt: window, lockedUntil }); },
    async password(id, hash) { Object.assign(state.accounts.get(id).credential, { passwordHash: hash, failedLoginCount: 0, failedLoginWindowAt: null, lockedUntil: null }); },
    async rename(id, fullName) { const a = state.accounts.get(id); a.fullName = fullName; a.updatedAt = clock.now(); return copy(a); },
  };
  const records = {
    async challenge(id) { return copy(state.challenges.get(id)); },
    async latest(userId, purpose) { return copy([...state.challenges.values()].filter(c => c.userId === userId && c.purpose === purpose).sort((a,b) => b.createdAt - a.createdAt)[0]); },
    async createChallenge(c) { state.challenges.set(c.id, copy(c)); },
    async updateChallenge(id, patch) { Object.assign(state.challenges.get(id), patch); },
    async invalidateChallenges(userId, purpose, now) { for (const c of state.challenges.values()) if (c.userId === userId && c.purpose === purpose && !c.consumedAt && !c.invalidatedAt) c.invalidatedAt = now; },
    async refresh(hash) { return copy([...state.tokens.values()].find(t => t.tokenHash === hash)); },
    async createRefresh(t) { state.tokens.set(t.id, copy(t)); },
    async revokeToken(id, now) { state.tokens.get(id).revokedAt = now; },
    async revokeFamily(userId, familyId, now) { for (const t of state.tokens.values()) if (t.userId === userId && t.familyId === familyId) t.revokedAt ??= now; },
    async revokeAll(userId, now) { for (const t of state.tokens.values()) if (t.userId === userId) t.revokedAt ??= now; },
    async liveFamily(userId, familyId, now) { return [...state.tokens.values()].some(t => t.userId === userId && t.familyId === familyId && !t.revokedAt && t.expiresAt > now); },
  };
  return {
    get state() { return state; },
    uow: {
      async read(work) { return work(accounts, {}); },
      async run(_key, work) {
        const previous = chain;
        let release; chain = new Promise(resolve => { release = resolve; });
        await previous;
        const before = structuredClone(state);
        try { return await work(accounts, {}); } catch (e) { state = before; throw e; } finally { release(); }
      },
    },
    repository: { in() { return records; } },
  };
}
