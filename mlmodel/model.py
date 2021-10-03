# -*- coding: utf-8 -*-
"""
Created on Sat Oct  2 20:57:41 2021

@author: k.herashchenko
"""

from pmdarima.arima import auto_arima
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def view_fit(model, X, city_data):
    forecast = model.predict_in_sample(X)

    city_data.index = city_data['date']
    forecast = pd.DataFrame(forecast)
    forecast.index = city_data['date']

    fig, ax = plt.subplots(figsize=(12, 7))
    kws = dict(marker='o')
    ax.plot(city_data['new_cases'], label='Train', **kws)
    ax.plot(forecast, label='Forecast', **kws)
    ax.plot(city_data['c3_cancel'] * 1000, label='Prediction', ls='--', linewidth=3)


def calculate_risk(city='Los Angeles', places=[0, 1, 4, 0, 3, 0, 5, 2, 2]):
    train_data = pd.read_csv('./train_data.csv', parse_dates=True)
    city_data = train_data[train_data['Admin2'] == city]
    city_data['date'] = pd.to_datetime(city_data['date'])

    last_c3_cancel = 2 - city_data.loc[city_data['date'].idxmax()]['c3_cancel']
    # last_c3_cancel = 2
    total_places = sum(places)
    total_squares = len(places)

    X = total_places * (2 - city_data['c3_cancel'].values.reshape(-1, 1))
    model = auto_arima(city_data['new_cases'], X, start_p=0, start_q=0)

    # view_fit(model, X, city_data)

    risks = []
    for pl in places:
        input_coef = last_c3_cancel * pl * total_squares
        risks.append(model.predict(n_periods=1, X=np.array(input_coef).reshape(-1, 1))[0])

    return risks

if __name__ == "__main__":
    result = calculate_risk()
    print(result)