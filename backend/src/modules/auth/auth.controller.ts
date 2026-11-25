import { Body, Controller, Get, HttpCode, Inject, Patch, Post, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiBody, ApiCreatedResponse, ApiNoContentResponse, ApiOkResponse, ApiResponse, ApiTags } from '@nestjs/swagger';
import { ApiErrorEnvelopeDto } from '../health/health.dto.js';
import { AuthApplicationService } from './auth.service.js';
import { AccessGuard, AuthThrottleGuard } from './auth.guard.js';
import type { AuthenticatedRequest } from './auth.guard.js';
import { ChallengeEnvelopeDto, ChallengeIdDto, IdentityDto, LoginDto, LogoutDto, MeEnvelopeDto, RefreshDto, RegisterDto, ResetPasswordDto, SessionEnvelopeDto, UpdateMeDto, VerificationEnvelopeDto, VerifyOtpDto } from './auth.dto.js';

@ApiTags('auth')
@ApiResponse({ status: 400, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 401, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 403, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 409, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 422, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 429, type: ApiErrorEnvelopeDto, description: 'Rate limited; Retry-After seconds.' })
@ApiResponse({ status: 503, type: ApiErrorEnvelopeDto })
@UseGuards(AuthThrottleGuard)
@Controller('auth')
export class AuthController {
  constructor(@Inject(AuthApplicationService) private readonly auth: AuthApplicationService) {}

  @Post('register') @ApiBody({ type: RegisterDto }) @ApiCreatedResponse({ type: ChallengeEnvelopeDto })
  register(@Body() body: RegisterDto) { return this.auth.register(body); }

  @Post('verify-otp') @ApiBody({ type: VerifyOtpDto }) @HttpCode(200) @ApiOkResponse({ type: VerificationEnvelopeDto })
  verifyOtp(@Body() body: VerifyOtpDto) { return this.auth.verifyOtp(body.challengeId, body.code); }

  @Post('resend-otp') @ApiBody({ type: ChallengeIdDto }) @HttpCode(200) @ApiOkResponse({ type: ChallengeEnvelopeDto })
  resendOtp(@Body() body: ChallengeIdDto) { return this.auth.resendOtp(body.challengeId); }

  @Post('login') @ApiBody({ type: LoginDto }) @HttpCode(200) @ApiOkResponse({ type: SessionEnvelopeDto })
  login(@Body() body: LoginDto) { return this.auth.login(body); }

  @Post('refresh') @ApiBody({ type: RefreshDto }) @HttpCode(200) @ApiOkResponse({ type: SessionEnvelopeDto })
  refresh(@Body() body: RefreshDto) { return this.auth.refresh(body.refreshToken); }

  @Post('logout') @ApiBody({ type: LogoutDto }) @UseGuards(AccessGuard) @ApiBearerAuth() @HttpCode(204) @ApiNoContentResponse()
  logout(@Req() request: AuthenticatedRequest, @Body() body: LogoutDto) { return this.auth.logout(request.principal, body.scope); }

  @Post('forgot-password') @ApiBody({ type: IdentityDto }) @HttpCode(200) @ApiOkResponse({ type: ChallengeEnvelopeDto })
  forgotPassword(@Body() body: IdentityDto) { return this.auth.forgotPassword(body); }

  @Post('reset-password') @ApiBody({ type: ResetPasswordDto }) @HttpCode(204) @ApiNoContentResponse()
  resetPassword(@Body() body: ResetPasswordDto) { return this.auth.resetPassword(body); }
}

// This HTTP adapter is wired by AuthModule to reuse its guard; all account table
// access is still through the exported Users domain unit of work.
@ApiTags('users') @ApiBearerAuth()
@ApiResponse({ status: 400, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 401, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 403, type: ApiErrorEnvelopeDto })
@ApiResponse({ status: 429, type: ApiErrorEnvelopeDto })
@UseGuards(AuthThrottleGuard, AccessGuard)
@Controller('users')
export class UsersController {
  constructor(@Inject(AuthApplicationService) private readonly auth: AuthApplicationService) {}
  @Get('me') @ApiOkResponse({ type: MeEnvelopeDto })
  me(@Req() request: AuthenticatedRequest) { return this.auth.getMe(request.principal); }
  @Patch('me') @ApiBody({ type: UpdateMeDto }) @ApiOkResponse({ type: MeEnvelopeDto })
  updateMe(@Req() request: AuthenticatedRequest, @Body() body: UpdateMeDto) { return this.auth.updateMe(request.principal, body.fullName); }
}
