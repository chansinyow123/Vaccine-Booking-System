using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Validation
{
    public class DayRangeAttribute : ValidationAttribute
    {
        private readonly int _min;
        private readonly int _max;

        public DayRangeAttribute(int min, int max) : base($"range must between {DateTime.Now.AddDays(min)} to {DateTime.Now.AddDays(max)} day.")
        {
            _min = min;
            _max = max;
        }

        public override bool IsValid(object value)
        {
            // Cast to value to datetime
            var datetime = (DateTime)value;
            var startDateTime = DateTime.Now.AddDays(_min);
            var endDateTime = DateTime.Now.AddDays(_max);

            return datetime >= startDateTime && datetime < endDateTime;
        }
    }
}
