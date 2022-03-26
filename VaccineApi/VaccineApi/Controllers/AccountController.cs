using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Account;
using VaccineApi.Services;
using VaccineApi.Static;

namespace VaccineApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly ITokenService _tokenService;
        private readonly IMapper _mapper;
        private readonly IFileService _fileService;

        public AccountController(UserManager<ApplicationUser> userManager, ITokenService tokenService, IMapper mapper, IFileService fileService)
        {
            _userManager = userManager;
            _tokenService = tokenService;
            _mapper = mapper;
            _fileService = fileService;
        }

        [HttpPost]
        [Route("login")]
        public async Task<IActionResult> Login([FromBody] LoginModel model)
        {
            // Find if username exist in database ----------------------------------------------------------------------------------------
            var user = await _userManager.FindByNameAsync(model.Username);
            if (user != null && await _userManager.CheckPasswordAsync(user, model.Password))
            {
                // check if email is verified or not -------------------------------------------------------------------------------------
                if (!await _userManager.IsEmailConfirmedAsync(user)) return NotFound();

                // Get User Role ---------------------------------------------------------------------------------------------------------
                var userRoles = await _userManager.GetRolesAsync(user);

                // Initialize Claims -----------------------------------------------------------------------------------------------------
                var authClaims = new List<Claim>
                {
                    new Claim(ClaimTypes.NameIdentifier, user.UserName),
                    new Claim(ClaimTypes.Name, user.UserName),
                    new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
                };

                // Add Role Claims -------------------------------------------------------------------------------------------------------
                var userRole = userRoles[0];
                authClaims.Add(new Claim(ClaimTypes.Role, userRole));

                // Generate Access Token -------------------------------------------------------------------------------------------------
                var token = _tokenService.GenerateAccessToken(authClaims);

                return Ok(new LoginResponseModel { 
                    Token       = token, 
                    UserRole    = userRole,
                    Name        = user.Name,
                    Email       = user.Email,
                    Address     = user.Address,
                    File        = user.File,
                    ContentType = user.ContentType
                });
            }

            return Unauthorized();
        }

        [HttpGet("get-profile")]
        [Authorize]
        public async Task<IActionResult> GetProfile()
        {
            // get the user from database ---------------------------------------------------------
            var email = User.Identity.Name;
            var user = await _userManager.FindByNameAsync(email);
            if (user == null) return NotFound();

            var model = _mapper.Map<UserModel>(user);

            return Ok(model);
        }

        [HttpGet("{id}")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> GetProfileById(string id)
        {
            // get the user from database ---------------------------------------------------------
            var user = await _userManager.FindByIdAsync(id);
            if (user == null) return NotFound();

            var model = _mapper.Map<UserModel>(user);

            return Ok(model);
        }

        [HttpGet("customers")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> GetAllCustomers() => 
            Ok(await GetUserModelList(UserRoles.Customer));

        [HttpGet("clinics")]
        [Authorize]
        public async Task<IActionResult> GetAllClinics() => 
            Ok(await GetUserModelList(UserRoles.Clinic));

        private async Task<List<UserModel>> GetUserModelList(string role)
        {
            // get all the account
            var user = await _userManager.GetUsersInRoleAsync(role)
                as List<ApplicationUser>;

            // filter non deleted account
            var data = user.Where(u => u.DeleteDateTime == null).OrderBy(u => u.Email);

            // map the UserModel
            return _mapper.Map<List<UserModel>>(data);
        }

        [HttpGet("customers/deleted")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> GetAllDeletedCustomers() =>
            Ok(await GetDeletedUserModelList(UserRoles.Customer));

        [HttpGet("clinics/deleted")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> GetAllDeletedClinics() =>
            Ok(await GetDeletedUserModelList(UserRoles.Clinic));

        private async Task<List<UserModel>> GetDeletedUserModelList(string role)
        {
            // get all the account
            var user = await _userManager.GetUsersInRoleAsync(role)
                as List<ApplicationUser>;

            // filter non deleted account
            var data = user.Where(u => u.DeleteDateTime != null);

            // map the UserModel
            return _mapper.Map<List<UserModel>>(data);
        }
    }
}
