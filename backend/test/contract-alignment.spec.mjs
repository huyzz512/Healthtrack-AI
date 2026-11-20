import { readFile } from 'node:fs/promises';

const schema = await readFile(new URL('../prisma/schema.prisma', import.meta.url), 'utf8');
const contract = await readFile(
  new URL('../../docs/api/integration-contract.md', import.meta.url),
  'utf8',
);

const releasedEnums = {
  AlertSeverity: ['INFO', 'WARNING', 'CRITICAL'],
  ConsentStatus: ['GRANTED', 'REVOKED'],
  ConsentType: [
    'AI_HEALTH_CONTEXT',
    'AI_MEDICAL_DOCUMENT',
    'AI_REPORT_SUMMARY',
  ],
  DataSource: ['USER_INPUT', 'HEALTH_CONNECT', 'IMPORTED', 'SYSTEM_GENERATED'],
  MoodType: ['HAPPY', 'NORMAL', 'SAD', 'STRESSED', 'ANXIOUS'],
  ReportStatus: ['REQUESTED', 'GENERATING', 'READY', 'FAILED'],
};

function prismaEnumValues(name) {
  const match = schema.match(new RegExp(`enum ${name} \\{([\\s\\S]*?)\\n\\}`));
  if (!match) return [];
  return match[1]
    .split('\n')
    .map((line) => line.trim().split(/\s+/)[0])
    .filter(Boolean);
}

describe('Foundation shared contract alignment', () => {
  for (const [name, values] of Object.entries(releasedEnums)) {
    it(`${name} matches the released contract and Prisma`, () => {
      expect(prismaEnumValues(name)).toEqual(values);
      expect(contract).toContain(`${name}: ${values.join(' | ')}`);
    });
  }
});
