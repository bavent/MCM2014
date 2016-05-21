import numpy as np
import scipy as sp 
import scipy.stats
import matplotlib.pyplot as plt

from collections import namedtuple
from collections import defaultdict

Team = namedtuple('Team', 'Name Year Coach1 Coach2 nRank pRank aRank')
Year = namedtuple('Year', 'Y nInv pInv aInv')

dataRoot = 'PythonData_FB/'
#dataRoot = 'PythonData_BB/'

# (For each year)
# Read in data
coachTeams = defaultdict(list)
for y in range(1913,2013):
	fileName = dataRoot + 'DATA_' + str(y) + '.csv'
	teams = []
	year = None

	first = True
	for line in open(fileName):

		# Burn the first line
		if(first):
			first = False
			continue

		data = line.split(',')
		teams.append(Team(data[0], y, data[1], data[2], int(data[3]), int(data[5]), int(data[7])))
		if year == None:
			year = Year(int(y), int(data[4]), int(data[6]), int(data[8]))

	# Normalize data
	# Rankings go from [0,1]
	nTeams = len(teams)
	normTeams = []
	for t in teams:
		nRank = float(nTeams - t.nRank + 1) / float(nTeams)
		pRank = float(nTeams - t.pRank + 1) / float(nTeams)
		aRank = float(nTeams - t.aRank + 1) / float(nTeams)
		normTeams.append(Team(t.Name, t.Year, t.Coach1, t.Coach2, nRank, pRank, aRank))
	teams = normTeams

	# Collect results by coach
	for t in teams:
		coachTeams[t.Coach1].append(t)
		if(t.Coach2 != ''):
			coachTeams[t.Coach2].append(t)

coachRanks = []

for coach, teamsList in coachTeams.iteritems():

	avgVal = 0
	for t in teamsList:
		avgVal += t.aRank
	avgVal = avgVal / len(teamsList)

	#print((coach, avgVal))
	coachRanks.append((coach, avgVal))

coachRanksArr = np.array(coachRanks, dtype=[('name', 'S40'),('score', float)])
coachRanksArr = np.sort(coachRanksArr, order='score')
for c in coachRanksArr:
	print(c)
	for t in coachTeams[c[0]]:
		print("-----" + str(t))