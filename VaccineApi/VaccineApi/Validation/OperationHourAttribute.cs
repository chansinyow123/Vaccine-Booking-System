using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Validation
{
    public class OperationHourAttribute : ValidationAttribute
    {
        public OperationHourAttribute() : base("The time is not within the operation hour.")
        { 
        }

        public override bool IsValid(object value)
        {
            // Cast to value to datetime
            var datetime = (DateTime)value;
            var timespan = datetime.TimeOfDay;

            var list = new List<TimeSpan>
            {
                new TimeSpan(10, 0, 0),
                new TimeSpan(11, 0, 0),
                new TimeSpan(13, 0, 0),
                new TimeSpan(14, 0, 0),
                new TimeSpan(15, 0, 0),
                new TimeSpan(16, 0, 0),
            };

            // if is within the operational hours, return true
            // else return false
            return list.Any(t => t.Hours == timespan.Hours && t.Minutes == timespan.Minutes && t.Seconds == timespan.Seconds);
        }
    }
}
