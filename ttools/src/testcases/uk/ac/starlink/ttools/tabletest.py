import stilts
import java
import uk
import StringIO
import cStringIO

messier = stilts.tread(testdir + "/messier.xml")

class TableTest(unittest.TestCase):

    def testObj(self):
        cols = messier.columns()
        self.assertEquals('Name', str(cols[0]))
        self.assertEquals('ImageURL', cols[-1].getName())
        colNgc = cols[2]
        self.assertEquals('NGC', str(colNgc))

        self.assertEquals('messier.xml', messier.parameters()['DemoLoc'])
        mp = messier.cmd_setparam('-type', 'float',
                                  '-unit', 'pint',
                                  'volume', '3.5')
        self.assertEquals(3.5, mp.parameters()['volume'])
        volparam = mp.getParameterByName('volume')
        self.assertEquals('pint', volparam.getInfo().getUnitString())
        self.assertEquals(java.lang.Float(0).getClass(),
                          volparam.getInfo().getContentClass())

        self.assert_(messier.isRandom())
        self.assertEquals(110, len(messier))
        for (ir, row) in enumerate(messier):
            self.assertEquals(row, messier[ir])
            for (ic, cell) in enumerate(row):
                self.assertEquals(cell, messier[ir][ic])
            self.assertEquals('M', row[0][0])
            self.assertEquals('M', row['Name'][0])
            self.assertEquals('.jpg', row[-1][-4:])
            self.assertEquals('.jpg', row['ImageURL'][-4:])
        head1 = messier.cmd_head(1)
        tail1 = messier.cmd_tail(1)
        self.assertEquals(1, len(head1))
        self.assertEquals(1, len(tail1))
        self.assertEquals(messier[0], head1[0])
        self.assertEquals(messier[-1], tail1[0])


        self.assertEqualTable(3*messier, messier+messier+messier)
        self.assertEqualTable(3*messier, messier*3)

        self.assertEquals('M23', messier[22][0])
        self.assertEquals('M23', messier[22]['Name'])
        self.assertEquals(6494, int(messier[22]['NGC']))
        self.assertEquals(6494, int(messier[22][colNgc]))
        self.assertEquals('.jpg', messier[5]['ImageURL'][-4:])
        self.assertEquals('.jpg', messier[5][-1][-4:])

    def testFilters(self):
        ands = (messier.cmd_select('equals("And",CON)')
                       .cmd_sort('-down', 'ID')
                       .cmd_keepcols('NAME'))
        self.assertEquals(['M110','M32','M31'],
                          [str(row[0]) for row in ands])

        self.assertEquals('M101', messier[100]['Name'])
        self.assertEquals('M101',
                          messier.cmd_addcol('Name', '999')[100]['Name'])
        self.assertEquals(999,
                          messier.cmd_addcol('-before', '1', 'Name', 999)
                                  [100]['Name'])

    def testTasks(self):
        self.assertEquals(99, int(stilts.calc('100-1')))
        self.assertEquals(31,
                          int(stilts.calc(table=messier
                                               .cmd_setparam("number", "29"),
                                          expression='2 + param$number')))

        self.assertEqualTable(2*messier, stilts.tcat([messier, messier]))
        self.assertEqualTable(2*messier,
                              stilts.tcatn(nin=2, in1=messier, in2=messier,
                                           countrows=True))

        m2 = stilts.tjoin(nin=2, in1=messier, in2=messier,
                          fixcols='all', suffix1='_A', suffix2='_B')
        self.assertEquals(len(m2.columns()), 2*len(messier.columns()))
        self.assertEqualData(m2.cmd_keepcols('*_A'), messier)

        self.assertEquals(['ID', 'lcol'],
                          [str(c) for c in
                               stilts.tcat(in_=[messier.cmd_keepcols(2)]*2,
                                           loccol='lcol').columns()])
        self.assertEquals(['ID'],
                          [str(c) for c in
                               stilts.tcat(in_=[messier.cmd_keepcols(2)]*2,
                                           loccol=None).columns()])

        self.assertRaises(SyntaxError, stilts.calc, '1+2', spurious='99')
        self.assertRaises(uk.ac.starlink.task.UsageException,
                          stilts.tmatchn)

    def testIO(self):
        for fmt in ['csv', 'fits', 'ascii', 'votable']:
            self.ioRoundTrip(messier, fmt)

    def ioRoundTrip(self, table, fmt):
        ofile = _UnclosedStringIO()
        table.write(ofile, fmt=fmt)
        ifile = cStringIO.StringIO(ofile.getvalue())
        table2 = stilts.tread(ifile, fmt=fmt)
        self.assertEqualTable(table, table2)

    def assertEqualTable(self, t1, t2):
        self.assertEquals([str(col) for col in t1.columns()],
                          [str(col) for col in t2.columns()])
        self.assertEqualData(t1, t2)

    def assertEqualData(self, t1, t2):
        try:
            self.assertEquals(len(t1), len(t2))
        except AttributeError:
            pass
        for ir, rows in enumerate(map(None, t1, t2)):
            self.assertEquals(rows[0], rows[1], "row %d" % ir)

    def runTest(self):
        tests = [value for key, value in vars(TableTest).iteritems()
                       if key.startswith('test') and callable(value)]
        for test in tests:
            test(self)

class _UnclosedStringIO(StringIO.StringIO):
    def __init__(self):
        StringIO.StringIO.__init__(self)
    def close(self):
        pass

TableTest().runTest()
