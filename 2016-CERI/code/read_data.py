import pandas as pd
import datetime


# transform into date types
def calc_fulldate(x):
    return datetime.datetime.strptime(x, "%Y-%m-%dT%H:%M:%S.%fZ")


def calc_hourweekday(x):
    return (x.weekday()*24) + x.hour


def calc_hour(x):
    return x.hour


def calc_month(x):
    return x.month


def calc_date(x):
    return x.date()

def load_clicks(data_folder): 

	# Session ID – the id of the session. In one session there are one or many clicks.
	# Timestamp – the time when the click occurred.
	# Item ID – the unique identifier of the item.
	# Category – the category of the item.
	clicks = pd.read_csv(data_folder+'/yoochoose-clicks.dat', usecols=[0, 1, 2])
	clicks.columns = ['session_id', 'timestamp', 'item_id']

	clicks['fulldate'] = clicks.timestamp.apply(calc_fulldate)
	clicks['hourweekday'] = clicks.fulldate.apply(calc_hourweekday)
	clicks['hour'] = clicks.fulldate.apply(calc_hour)
	clicks['month'] = clicks.fulldate.apply(calc_month)
	clicks['date'] = clicks.fulldate.apply(calc_date)
	return clicks


def load_buys(data_folder): 
	# Session ID - the id of the session. In one session there are one or many buying events.
	# Timestamp - the time when the buy occurred.
	# Item ID – the unique identifier of item.
	# Price – the price of the item.
	# Quantity – how many of this item were bought.
	buys = pd.read_csv(data_folder+'/yoochoose-buys.dat')
	buys.columns = ['session_id', 'timestamp', 'item_id', 'price', ' quantity']
	buys['purchase']= True
	buys['fulldate'] = buys.timestamp.apply(calc_fulldate)
	buys['hourweekday'] = buys.fulldate.apply(calc_hourweekday)
	buys['hour'] = buys.fulldate.apply(calc_hour)
	buys['month'] = buys.fulldate.apply(calc_month)
	buys['date'] = buys.fulldate.apply(calc_date)
	return buys

def load_categories(data_folder): 
	# READ CATEGORIES
	clicks_categories = pd.read_csv(data_folder+'/categories.dat')
	clicks_categories.columns = ['item_id', 'category']
	return clicks_categories
