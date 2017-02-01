DAYS_IN_MONTH = [31,28,31,30,31,30,31,31,30,31,30,31]
DAYS_IN_MONTH_COMM = [None for i in range(12)]
DOW = ['sun', 'mon', 'tues', 'wed', 'thurs', 'fri', 'sat']

# pre-calculate cumulative days till this month (not including this month).
for i in range(12):
    if i == 0: DAYS_IN_MONTH_COMM[i] = 0
    else: DAYS_IN_MONTH_COMM[i] = DAYS_IN_MONTH_COMM[i-1] + DAYS_IN_MONTH[i-1]

def year_component(year):
    year = year - 1 # don't count this year.
    years = year
    years4 = year / 4
    years100 = year / 100
    years400 = year / 400
    nonleaps = years - years4 + years100 - years400
    leaps = years - nonleaps
    days = years * 365 + leaps
    return days

def month_component(month):
    return DAYS_IN_MONTH_COMM[month - 1]

def day_component(day):
    return day

def is_leap_year(y):
    if y % 4 != 0: return False # normal year
    if y % 100 != 0: return True # not every 100 years
    if y % 400 != 0: return False # not every 400 years
    return True

def weekday(year, month, day):
    days = year_component(year) + month_component(month) + day_component(day)
    if month > 2 and is_leap_year(year): days += 1
    return DOW[(days) % 7]
            
print weekday(1301, 1, 1) == 'sat'
print weekday(1701, 1, 1) == 'sat'
print weekday(1799, 1, 1) == 'tues'
print weekday(1801, 1, 1) == 'thurs'
print weekday(1899, 1, 1) == 'sun'
print weekday(1901, 1, 1) == 'tues'
print weekday(1998, 1, 1) == 'thurs'
print weekday(1999, 1, 1) == 'fri'
print weekday(2013, 11, 1) == 'fri'
print weekday(2013, 1, 1) == 'tues'
print weekday(2017, 1, 31) == 'tues'
print weekday(2017, 2, 1) == 'wed'
print weekday(2017, 2, 2) == 'thurs'
