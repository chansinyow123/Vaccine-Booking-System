using Microsoft.AspNetCore.Http;
using System.Collections.Generic;
using System.Security.Claims;

namespace VaccineApi.Services
{
    public interface ITokenService
    {
        string GenerateAccessToken(IEnumerable<Claim> authClaims);
    }
}