using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using VaccineApi.Config;

namespace VaccineApi.Services
{
    public class TokenService : ITokenService
    {
        private readonly JWTConfig _JWTConfig;

        public TokenService(IOptionsSnapshot<JWTConfig> JWTConfig)
        {
            _JWTConfig = JWTConfig.Value;
        }

        public string GenerateAccessToken(IEnumerable<Claim> authClaims)
        {
            // Sign the key using the secret in appsetting.json file
            var authSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_JWTConfig.Secret));

            // Declare jwt token
            var jwtToken = new JwtSecurityToken(
                issuer: _JWTConfig.ValidIssuer,
                audience: _JWTConfig.ValidAudience,
                claims: authClaims,
                notBefore: DateTime.Now,
                expires: DateTime.Now.AddHours(_JWTConfig.JWTExpireHours),
                signingCredentials: new SigningCredentials(authSigningKey, SecurityAlgorithms.HmacSha256)
            );

            // return the encoded jwt token
            return new JwtSecurityTokenHandler().WriteToken(jwtToken);
        }

        // If we want to decode the header or payload in JWT Token
        //public IActionResult Decode(string part)
        //{
        //    var bytes = Convert.FromBase64String(part);
        //    return Ok(Encoding.UTF8.GetString(bytes));
        //}
    }
}
