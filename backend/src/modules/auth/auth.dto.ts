import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsIn, IsNotEmpty, IsOptional, IsString, IsUUID, Matches, MaxLength } from 'class-validator';

export class IdentityDto {
  @ApiPropertyOptional({ type: String, maxLength: 320, description: 'Exactly one of email or phone. Trimmed and lowercased by Backend.' })
  @IsOptional() @IsString() @MaxLength(320)
  email?: string;

  @ApiPropertyOptional({ type: String, maxLength: 32, description: 'Vietnam phone: ten digits starting with 0. Exactly one identity.' })
  @IsOptional() @IsString() @MaxLength(32)
  phone?: string;
}
export class LoginDto extends IdentityDto {
  @ApiProperty({ type: String, writeOnly: true, maxLength: 128 })
  @IsString() @IsNotEmpty() @MaxLength(128)
  password!: string;
}
export class RegisterDto extends LoginDto {
  @ApiProperty({ type: String, maxLength: 160, description: '2–80 Unicode code points after trimming.' })
  @IsString() @MaxLength(160)
  fullName!: string;

  @ApiProperty({ type: String, writeOnly: true, maxLength: 128 })
  @IsString() @MaxLength(128)
  confirmPassword!: string;
}
export class ChallengeIdDto {
  @ApiProperty({ type: String, format: 'uuid' })
  @IsUUID()
  challengeId!: string;
}
export class VerifyOtpDto extends ChallengeIdDto {
  @ApiProperty({ type: String, pattern: '^[0-9]{6}$', writeOnly: true })
  @IsString() @Matches(/^[0-9]{6}$/)
  code!: string;
}
export class RefreshDto {
  @ApiProperty({ type: String, writeOnly: true, pattern: '^[A-Za-z0-9_-]{43}$' })
  @IsString() @Matches(/^[A-Za-z0-9_-]{43}$/)
  refreshToken!: string;
}
export class LogoutDto {
  @ApiProperty({ enum: ['CURRENT', 'ALL'] })
  @IsIn(['CURRENT', 'ALL'])
  scope!: 'CURRENT' | 'ALL';
}
export class UpdateMeDto {
  @ApiProperty({ type: String, maxLength: 160, description: '2–80 Unicode code points after trimming.' })
  @IsString() @MaxLength(160)
  fullName!: string;
}
export class ResetPasswordDto {
  @ApiProperty({ type: String, maxLength: 512, description: 'Opaque recovery handle returned by forgot-password, not a registration UUID.' })
  @IsString() @MaxLength(512)
  challengeId!: string;

  @ApiProperty({ type: String, pattern: '^[0-9]{6}$', writeOnly: true })
  @IsString() @Matches(/^[0-9]{6}$/)
  code!: string;

  @ApiProperty({ type: String, writeOnly: true, maxLength: 128 })
  @IsString() @MaxLength(128)
  password!: string;

  @ApiProperty({ type: String, writeOnly: true, maxLength: 128 })
  @IsString() @MaxLength(128)
  confirmPassword!: string;
}
export class ChallengeResponseDto {
  @ApiProperty({ type: String, description: 'Registration UUID or opaque encrypted recovery handle.' }) challengeId!: string;
  @ApiProperty({ type: String, format: 'date-time' }) expiresAt!: string;
  @ApiProperty({ type: String, format: 'date-time' }) resendAvailableAt!: string;
  @ApiProperty({ type: Number, minimum: 0, maximum: 3, description: 'Recovery always returns 0; request recovery again after the displayed wait.' }) resendsRemaining!: number;
}
export class SessionResponseDto {
  @ApiProperty({ type: String }) accessToken!: string;
  @ApiProperty({ type: String }) refreshToken!: string;
  @ApiProperty({ enum: ['Bearer'] }) tokenType!: 'Bearer';
  @ApiProperty({ type: String, format: 'date-time' }) accessTokenExpiresAt!: string;
  @ApiProperty({ type: String, format: 'date-time' }) refreshTokenExpiresAt!: string;
}
export class MeResponseDto {
  @ApiProperty({ type: String, format: 'uuid' }) id!: string;
  @ApiProperty({ type: String }) fullName!: string;
  @ApiProperty({ type: String, nullable: true }) email!: string | null;
  @ApiProperty({ type: String, nullable: true }) phone!: string | null;
  @ApiProperty({ type: String, format: 'date-time', nullable: true }) emailVerifiedAt!: string | null;
  @ApiProperty({ type: String, format: 'date-time', nullable: true }) phoneVerifiedAt!: string | null;
  @ApiProperty({ type: String, format: 'date-time' }) createdAt!: string;
  @ApiProperty({ type: String, format: 'date-time' }) updatedAt!: string;
}
export class VerificationResponseDto { @ApiProperty({ type: Boolean, enum: [true] }) verified!: boolean; }
export class ChallengeEnvelopeDto { @ApiProperty({ type: ChallengeResponseDto }) data!: ChallengeResponseDto; }
export class SessionEnvelopeDto { @ApiProperty({ type: SessionResponseDto }) data!: SessionResponseDto; }
export class MeEnvelopeDto { @ApiProperty({ type: MeResponseDto }) data!: MeResponseDto; }
export class VerificationEnvelopeDto { @ApiProperty({ type: VerificationResponseDto }) data!: VerificationResponseDto; }
