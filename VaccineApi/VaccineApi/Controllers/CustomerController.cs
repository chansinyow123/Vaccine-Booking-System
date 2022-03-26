using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Customer;
using VaccineApi.Services;
using VaccineApi.Static;

namespace VaccineApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class CustomerController : ControllerBase
    {
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly IEmailService _emailService;
        private readonly IFileService _fileService;
        private readonly IMapper _mapper;

        public CustomerController(UserManager<ApplicationUser> userManager, IEmailService emailService, IFileService fileService, IMapper mapper)
        {
            _userManager = userManager;
            _emailService = emailService;
            _fileService = fileService;
            _mapper = mapper;
        }

        [HttpPost("")]
        public async Task<IActionResult> RegisterCustomer([FromForm] RegisterCustomerModel model)
        {
            // Check if user already exist in database ----------------------------------------------
            var userExists = await _userManager.FindByNameAsync(model.Username);
            if (userExists != null)
            {
                ModelState.AddModelError(nameof(model.Username), "User Already Exist");
                return ValidationProblem(ModelState);
            }

            // Create User Instance ----------------------------------------------------------------------------
            ApplicationUser user = new ApplicationUser()
            {
                Name = model.Name,
                Email = model.Username,
                UserName = model.Username,
                PhoneNumber = model.PhoneNumber,
                IC = model.IC,
                Address = model.Address
                //File = photoContent,
                //ContentType = registerModel.Image.ContentType
            };

            // If Image is not null, Get the Photo MemoryStream and put into database ----------------------------
            if (model.File != null)
            {
                // specify the permittedExtension -------------------------------------------------------
                string[] permittedExtensions = { ".jpg", ".jpeg", ".png" };
                long fileSizeLimit = 20971520; // 20mb

                var fileByte = await _fileService.ProcessFormFile(model.File, ModelState, permittedExtensions, fileSizeLimit);

                // if error in file, return and display them --------------------------------------------
                if (!ModelState.IsValid)
                    return ValidationProblem(ModelState);

                // save file byte data to database ------------------------------------------------------
                user.File = fileByte;
                user.ContentType = model.File.ContentType;
            }

            // Create User in database ---------------------------------------------------------------------------
            var result = await _userManager.CreateAsync(user, model.Password);
            if (!result.Succeeded)
            {
                // loop through error and put into modelstate
                foreach (var e in result.Errors) 
                    ModelState.AddModelError(nameof(model.Password), e.Description);

                return ValidationProblem(ModelState);
            }

            // add to ST role ------------------------------------------------------------------------------------
            await _userManager.AddToRoleAsync(user, UserRoles.Customer);

            // Send Customer Email Verification ------------------------------------------------------------------
            await _emailService.SendEmailConfirmationAsync(user, model.DeepLink);

            return Ok();
        }

        [HttpPost("verify")]
        public async Task<IActionResult> VerifyAccount([FromBody] VerifyAccountModel model)
        {
            // Check is userId exist or not --------------------------------------------------------- 
            var user = await _userManager.FindByIdAsync(model.UserId);
            if (user == null) NotFound();

            // Confirm Email Verification -----------------------------------------------------------
            var result = await _userManager.ConfirmEmailAsync(user, model.Token);
            if (!result.Succeeded) NotFound();

            return Ok();
        }

        [HttpPut("")]
        [Authorize(Roles = UserRoles.Customer)]
        public async Task<IActionResult> EditCustomer([FromForm] EditCustomerModel model)
        {
            // get the user from database ---------------------------------------------------------
            var user = await _userManager.FindByNameAsync(User.Identity.Name);
            if (user == null) return NotFound();

            // assign updated property
            user.Name = model.Name;
            user.Address = model.Address;
            user.IC = model.IC;
            user.PhoneNumber = model.PhoneNumber;

            // if want to apply file upload
            if (model.IsFileUpload)
            {
                // If Image is not null, Get the Photo ByteArray and put into database ----------------------------
                if (model.File != null)
                {
                    // specify the permittedExtension -------------------------------------------------------
                    string[] permittedExtensions = { ".jpg", ".jpeg", ".png" };
                    long fileSizeLimit = 20971520; // 20mb

                    var fileByte = await _fileService.ProcessFormFile(model.File, ModelState, permittedExtensions, fileSizeLimit);

                    // if error in file, return and display them --------------------------------------------
                    if (!ModelState.IsValid)
                        return ValidationProblem(ModelState);

                    // save file byte data to database ------------------------------------------------------
                    user.File = fileByte;
                    user.ContentType = model.File.ContentType;
                }
                else
                {
                    // else set the file to null
                    user.File = null;
                    user.ContentType = null;
                }
            }

            // check if there is any error -------------------------------------------------------
            var result = await _userManager.UpdateAsync(user);
            if (!result.Succeeded) return StatusCode(StatusCodes.Status500InternalServerError);

            return Ok();
        }

        //[HttpGet("st")]
        //public async Task<IActionResult> GetUserPhoto([FromForm] RegisterModel signUpModel)
        //{
        //    var user = await _userManager.FindByNameAsync(signUpModel.Username);
        //    if (user == null)
        //    {
        //        return BadRequest(new { error = "Wrong username" });
        //    }

        //    // ASP.NET CORE Web API automatically convert byte array to base 64, use this code if wanted to enforce base64 for string concat:
        //    // var base64 = Convert.ToBase64String(user.Photo);
        //    return Ok(user.Image);

        //    // The File Return Type is used to generate or download the file when accessing this route.
        //    // return File(user.Photo, MediaTypeNames.Image.Jpeg);
        //}


    }
}
