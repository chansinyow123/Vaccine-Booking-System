using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Http.Features;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.IdentityModel.Tokens;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using VaccineApi.Config;
using VaccineApi.Data;
using VaccineApi.Services;
using VaccineApi.Static;

namespace VaccineApi
{
    public class Startup
    {
        private readonly IConfiguration _configuration;

        public Startup(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // This method gets called by the runtime. Use this method to add services to the container.
        // For more information on how to configure your application, visit https://go.microsoft.com/fwlink/?LinkID=398940
        public void ConfigureServices(IServiceCollection services)
        {
            // Add SQL Database ---------------------------------------------------------------------------------------------------
            services.AddDbContext<ApplicationDbContext>(options =>
                options.UseSqlServer(
                    _configuration.GetConnectionString("DefaultConnection")
                )
            );

            // Add Identity ------------------------------------------------------------------------------------------------------
            services.AddIdentity<ApplicationUser, IdentityRole>()
                .AddEntityFrameworkStores<ApplicationDbContext>()
                .AddDefaultTokenProviders();

            // Add Services ------------------------------------------------------------------------------------------------------
            services.AddScoped<ITokenService, TokenService>();
            services.AddScoped<IEmailService, EmailService>();
            services.AddScoped<IFileService, FileService>();
            services.AddAutoMapper(typeof(Startup));

            // Add Configuration -------------------------------------------------------------------------------------------------
            services.Configure<JWTConfig>(_configuration.GetSection("JWT"));
            services.Configure<MailKitConfig>(_configuration.GetSection("MailKit"));

            // Adding Authentication ---------------------------------------------------------------------------------------------
            services.AddAuthentication(options =>
            {
                // we check the bearer to confirm that we are authenticated 
                options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
                // Use this to check if we are allowed to do something
                options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
                // The fallback for scheme if above is failed
                options.DefaultScheme = JwtBearerDefaults.AuthenticationScheme;
                // Scheme can be "ClientCookie" or "OurServer"
            })
            // Adding Jwt Bearer -------------------------------------------------------------------------------------------------
            .AddJwtBearer(options =>
            {
                // if we want to use url to access the JWT token
                //options.Events = new JwtBearerEvents()
                //{
                //    // when receiving response -------------------------------------------------------------
                //    OnMessageReceived = context =>
                //    {
                //        // if access-token is on the HttpOnly Cookie, Use it
                //        if (context.Request.Cookies.ContainsKey("X-Access-Token"))
                //        {
                //            context.Token = context.Request.Cookies["X-Access-Token"];
                //        }
                //        return Task.CompletedTask;
                //    },
                //    // When JWT Token authentication failed ------------------------------------------------
                //    OnAuthenticationFailed = context =>
                //    {
                //        // if access-token is expired, add to response header
                //        if (context.Exception.GetType() == typeof(SecurityTokenExpiredException))
                //        {
                //            context.Response.Headers.Add("Token-Expired", "true");
                //        }
                //        return Task.CompletedTask;
                //    }
                //};

                options.SaveToken = true;
                options.RequireHttpsMetadata = true;

                // Get the JWT Config from appsetting.json file and then bind to JWTConfig class -----------
                var JWTConfig = _configuration.GetSection("JWT").Get<JWTConfig>();
                options.TokenValidationParameters = new TokenValidationParameters()
                {
                    ClockSkew = TimeSpan.Zero,  // By default, jwt expiration takes 5 minute
                    ValidateIssuer = true,
                    ValidateAudience = true,
                    ValidateLifetime = true,
                    ValidateIssuerSigningKey = true,
                    ValidAudience = JWTConfig.ValidAudience,
                    ValidIssuer = JWTConfig.ValidIssuer,
                    IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(JWTConfig.Secret))
                };
            });

            // Configure Identity Options ------------------------------------------------------------------------------------------
            services.Configure<IdentityOptions>(options =>
            {
                // Password settings.
                options.Password.RequireDigit = false;
                options.Password.RequireLowercase = false;
                options.Password.RequireNonAlphanumeric = false;
                options.Password.RequireUppercase = false;
                options.Password.RequiredLength = 6;
                options.Password.RequiredUniqueChars = 0;

                // User Settings
                options.User.RequireUniqueEmail = true;
            });

            // Only in Debug Purpose ---------------------------------------------------------------------------------------------
#if DEBUG
            // if forgot to migrate database, it will display migrate button on browser.
            services.AddDatabaseDeveloperPageExceptionFilter();
#endif

            // only access controller --------------------------------------------------------------------------------------------
            services.AddControllers().AddNewtonsoftJson(options =>
                options.SerializerSettings.ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore
            );

            // file upload configuration -----------------------------------------------------------------------------------------
            services.Configure<FormOptions>(options =>
            {
                // Set the limit to max
                options.MultipartBodyLengthLimit = int.MaxValue;
            });
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env, IServiceProvider serviceProvider)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
                app.UseMigrationsEndPoint();
            }
            else
            {
                // May not need it, since we use SPA for pages
                // app.UseExceptionHandler("/Home/Error");

                // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
                app.UseHsts();
            }

            app.UseRouting();

            // Who are you?
            app.UseAuthentication();
            // Are you allowed?
            app.UseAuthorization();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });

            CreateRoles(serviceProvider).Wait();
        }

        // When the apps first load, create the role in database, and also add a main Admin user
        // Here is the references link for this implementation: https://stackoverflow.com/questions/42471866/how-to-create-roles-in-asp-net-core-and-assign-them-to-users
        private async Task CreateRoles(IServiceProvider serviceProvider)
        {
            // Get the required service -------------------------------------------------------------------------------------
            var RoleManager = serviceProvider.GetRequiredService<RoleManager<IdentityRole>>();
            var UserManager = serviceProvider.GetRequiredService<UserManager<ApplicationUser>>();

            // Convert the Static UserRoles Class into List -----------------------------------------------------------------
            // Here is the references link for this implementation:
            // https://stackoverflow.com/questions/41477862/linq-get-the-static-class-constants-as-list
            Type type = typeof(UserRoles);
            List<string> roles = type.GetFields().Select(x => x.GetValue(null).ToString()).ToList();

            foreach (var roleName in roles)
            {
                // if role is not exist, create the roles and seed into database
                if (!await RoleManager.RoleExistsAsync(roleName))
                {
                    await RoleManager.CreateAsync(new IdentityRole(roleName));
                }
            }

            // Create an Admin User -------------------------------------------------------------------------------------------
            var admin = new ApplicationUser
            {
                Name = _configuration["Admin:Name"],
                UserName = _configuration["Admin:Username"],
                Email = _configuration["Admin:Username"],
            };

            // Create Admin if no admin found in database ---------------------------------------------------------------------
            var adminExist = await UserManager.FindByNameAsync(admin.UserName);

            if (adminExist == null)
            {
                string userPWD = _configuration["Admin:Password"];

                await UserManager.CreateAsync(admin, userPWD);
                await UserManager.AddToRoleAsync(admin, UserRoles.Admin);

                // verify email confirmation
                var token = await UserManager.GenerateEmailConfirmationTokenAsync(admin);
                await UserManager.ConfirmEmailAsync(admin, token);
            }

            // Testing Area for clinic and customer account ======================================================================
            // Clinic ------------------------------------------------------------------------------------------------------------
            var clinic = new ApplicationUser
            {
                UserName = "chansinyow2@gmail.com",
                Email = "chansinyow2@gmail.com",
                Address = "Selayang Jaya"
            };

            var clinicExist = await UserManager.FindByNameAsync(clinic.UserName);
            if (clinicExist == null)
            {
                string userPWD = _configuration["Admin:Password"];

                await UserManager.CreateAsync(clinic, userPWD);
                await UserManager.AddToRoleAsync(clinic, UserRoles.Clinic);

                // verify email confirmation
                var token = await UserManager.GenerateEmailConfirmationTokenAsync(clinic);
                await UserManager.ConfirmEmailAsync(clinic, token);
            }

            // Customer ----------------------------------------------------------------------------------------------------------
            var customer = new ApplicationUser
            {
                Name = "3 Chan Sin Yow",
                UserName = "chansinyow3@gmail.com",
                Email = "chansinyow3@gmail.com",
                PhoneNumber = "0121231232",
                IC = "000000101212",
                Address = "Selayang Jaya"
            };
            var customerExist = await UserManager.FindByNameAsync(customer.UserName);
            if (customerExist == null)
            {
                string userPWD = _configuration["Admin:Password"];

                await UserManager.CreateAsync(customer, userPWD);
                await UserManager.AddToRoleAsync(customer, UserRoles.Customer);

                // verify email confirmation
                var token = await UserManager.GenerateEmailConfirmationTokenAsync(customer);
                await UserManager.ConfirmEmailAsync(customer, token);
            }
        }
    }
}
