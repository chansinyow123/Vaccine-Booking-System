using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Data
{
    public class ApplicationDbContext : IdentityDbContext<ApplicationUser>
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options)
        {
        }

        public DbSet<Booking> Bookings { get; set; }
        public DbSet<Vaccine> Vaccines { get; set; }
        public DbSet<Dose> Doses { get; set; }
        public DbSet<Appointment> Appointments { get; set; }

        // For changing table name in database, and also set composite key
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Booking>().ToTable("Booking");
            modelBuilder.Entity<Vaccine>().ToTable("Vaccine");
            modelBuilder.Entity<Dose>().ToTable("Dose");
            modelBuilder.Entity<Appointment>().ToTable("Appointment");

            // Many-to-one relationship between vaccine and (clinic)user
            // The foreign key must not be null, and do not allow user to be deleted
            modelBuilder.Entity<Vaccine>()
                .HasOne(v => v.Clinic)
                .WithMany(c => c.Vaccines)
                .HasForeignKey(v => v.ClinicId)
                .IsRequired()
                .OnDelete(DeleteBehavior.Restrict);

            // Many-to-one relationship between booking and (customer)user
            // The foreign key must not be null, and do not allow user to be deleted
            modelBuilder.Entity<Booking>()
                .HasOne(b => b.Customer)
                .WithMany(c => c.Bookings)
                .HasForeignKey(b => b.CustomerId)
                .IsRequired()
                .OnDelete(DeleteBehavior.Restrict);

            //modelBuilder.Entity<CourseAssignment>()
            //    .HasKey(c => new { c.CourseID, c.InstructorID });
            base.OnModelCreating(modelBuilder);
        }
    }
}
