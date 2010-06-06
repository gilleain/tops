import re
class Analyzer(object):

    def analyze(self, string):
        (name, vertices, edges) = string.split(" ")
        sheets = self.getSheets(edges)
        print name, "has :", len(sheets), "sheets"
        for sheet in sheets:
            print sheet

    def getSheets(self, edges):
        edge_list = re.findall("(\d+):(\d+)(.)", edges)		
        sheets = []
        for edge in edge_list:
            self.addToSheets(edge, sheets)
        return sheets

    def addToSheets(self, edge, sheets):
        (first, second, type) = edge	
        if sheets == []:
            sheet = []
            sheet.append(first)
            sheet.append(second)
            sheets.append(sheet)
        else:
            for sheet in sheets:
                if first in sheet:
                    first_index = sheet.index(first)
                if second in sheet:
                    second_index = sheet.index(second)

if __name__=="__main__":
	import sys
	try:
		filename = sys.argv[1]
	except IndexError:
		print "type a filename of strings to analyze"
		sys.exit(0)
	
	analyzer = Analyzer()
	file = open(filename, 'r')
	for string in file:
		string = string.rstrip("\n")
		analyzer.analyze(string)	
