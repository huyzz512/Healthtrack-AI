export default {
  testEnvironment: 'node',
  testMatch: ['<rootDir>/test/**/*.spec.mjs'],
  transform: {},
  collectCoverageFrom: ['dist/**/*.js', '!dist/main.js'],
};
