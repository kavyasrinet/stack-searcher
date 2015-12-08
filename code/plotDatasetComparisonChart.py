import plotly.plotly as py
import plotly.graph_objs as go

trace2 = go.Scatter(
    y=[48.5,200,54.3,23.6,21.3,25.2,59.3,190,47.9,39,1.3],
    x=[14.39258083, 7.374300025, 10.64182307, 4.021608643, 6.581583438, 3.320923306, 15.8490566, 14.90211372, 9.019734714, 5.195872223, 15.35393819],
    mode='markers+text',
    name='Markers and Text',
    text=["Travel", "WordPress", "Academia", "History", "Parenting", "Movies", "Christianity", "Physics", "Cooking", "Money", "Coffee"],
    textposition='bottom'
)

data = [trace2]
layout = go.Layout(
    showlegend=False,
    xaxis=dict(
        title='Linked Questions (%)',
        titlefont=dict(
            family='Arial',
            size=18,
            color='#7f7f7f'
        )
    ),
    yaxis=dict(
        title='Size of Posts file (KB)',
        titlefont=dict(
            family='Arial',
            size=18,
            color='#7f7f7f'
        )
    )
)
fig = go.Figure(data=data, layout=layout)
plot_url = py.plot(fig, filename='text-chart-basic')