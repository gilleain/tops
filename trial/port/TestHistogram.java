package port;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestHistogram {
    
    @Test
    public void testParse() {
        String s = "|0.00:0.10:[2tgi00, 2bc2A0, 1gof00, 1amp00, 1onc00, 1ifc00, 8abp00, 1toaA0, 1nls00, 1whi00, 1kuh00, 1b9wA0, 1ql0A0, 1dptA0, 1lcl00, 1a3aD0, 1tph10, 1behB0, 3ezmA0, 1a6200, 2por00, 1bteA0, 1hmt00, 1qq5A0, 1a7s00, 1nwpA0, 1qq4A0, 3eipA0, 1ttbA0, 1bec00, 3proC0, 1gai00, 1svfA0, 7rsa00, 1cruB0, 1b8oA0, 1wapB0, 1dnlA0, 3btoA0, 1vcaA0, 2tnfA0, 1bu8A0, 1swuB0, 1mdc00, 1qgqA0, 1qj4A0, 2qwc00, 1ugiD0, 1dhn00, 1qnjA0, 1qlwA0, 1aohB0, 1auoA0, 1aqzA0, 1es5A0, 2cuaA0, 1qfmA0, 2bbkH0, 1agjA0, 1bf6B0, 1dvjA0, 7odcA0, 1b4kA0, 1mgtA0, 1qksA0, 1difA0, 1xnb00]|0.10:0.20:[1c1kA0, 1dlfL0, 7atjA0, 1bs900, 1bueA0, 1cxyA0, 1stn00, 1qgwB0, 1bbzE0, 1cex00, 1c3wA0, 1qtwA0, 1ceqA0, 1bkb00, 19hcA0, 1qqqA0, 1sluA0, 1orc00, 1a1yI0, 1msi00, 7fd1A0, 8rucA0, 2fdn00, 1jetA0, 1ubpA0, 1c5200, 1c90A0, 4lzt00, 2cyp00, 3vub00, 1bf4A0, 1dp7P0, 1rie00]|0.20:0.30:[1bdo00, 1fus00, 1g3p00, 2hft00, 1pcfA0]|0.30:0.40:[1ctf00, 1vcc00, 1gvp00, 1cv800, 2cpl00, 1nar00, 1kp6A0, 1yveI0, 7a3hA0, 1flmB0, 1rb900, 1fvkA0, 4pgaA0, 1gdoB0, 1rhs00, 1vns00, 1ads00, 1bfd00, 1qupA0, 1qf9A0, 1erxA0, 1yacB0, 1ajsA0, 1cpq00, 1mctI0, 1nkr00, 1nox00, 2ctc00, 2myr00, 2a0b00, 3cla00, 1cf9B0, 3seb00, 1ah700, 3grs00, 1a6m00, 1c3d00, 1bg200, 3sil00, 3stdA0, 1t1dA0, 1dosA0, 1oaa00, 6cel00, 1bx4A0, 1vhh00, 1brt00, 1bj700, 1bkrA0, 1ajj00, 1czfA0, 1cb0A0, 1iibA0, 1amm00, 153l00, 1uteA0, 2dri00, 1jer00, 1cydA0, 256bA0, 1axn00, 1qddA0, 1ay7B0, 1cmbA0, 1npk00, 1nulB0, 1cnzB0, 1ak000, 1mpgB0, 1ek0A0, 1c5eA0, 1smd00, 1gdj00, 1a8e00, 1uroA0, 1b4vA0, 1qh8A0, 1bw9A0, 2acy00, 2tpsA0, 1bk7A0, 2pth00, 1taxA0, 1zin00, 1gci00, 1qe3A0, 1dbwB0, 1dozA0, 1bfg00, 2ahjC0, 1gpeA0, 1cc8A0, 5p2100, 1plc00, 1a8d00, 1smlA0, 1bdmB0, 1ckeA0, 1nfn00, 5nul00, 1cipA0, 1ako00, 1c3pA0, 1ezm00, 1mfiA0, 1iab00, 1aayA0, 1lam00, 2trxA0, 1bgf00, 1osa00, 3chy00, 1lucB0, 1isuA0, 2igd00, 1qreA0, 1mml00, 1aba00, 1tca00, 1xikA0, 1qh5A0, 2ilk00, 2mhr00, 1sbp00, 1qnf00, 1fkj00, 1bd0A0, 3pyp00, 1hpm00, 16pk00, 1cjwA0, 1bxoA0, 1gceA0, 1nbcA0, 1cxc00, 1qh4A0, 1thv00, 1bqcA0, 6gsvB0, 1bkjA0, 1vfyA0, 1vie00, 1mroB0, 1cnv00, 1czpA0, 1kpf00, 1nif00, 1nzyB0, 1qsgG0, 1guqA0, 2sak00, 1pmi00, 1jhgA0, 2cba00, 1bk000, 1dciA0, 1hcl00, 1dfuP0, 1amf00, 1eco00, 1qrrA0, 1aquA0, 1dbgA0, 1aho00, 1cs1A0, 3nul00, 2hmzA0, 2rn200, 1akr00, 1ixh00, 2ayh00, 4xis00, 1tif00, 1tml00, 1bu7A0, 1erv00, 1a4iB0, 1pymA0, 1aqb00, 1mba00, 1wab00, 5icb00, 1qczA0, 1fnc00, 1atg00, 1ptf00, 1qjdA0, 1pgs00, 1flp00, 1qsaA0, 1ejgA0, 1mla00, 2hbg00, 1gca00, 1mrj00, 2lisA0, 1qb7A0, 1dgfA0, 1cxqA0, 1gd1O0, 1pdo00, 1mun00, 1ten00, 1msk00, 1elkA0, 1fdr00, 1lkkA0, 1phnA0, 1vfrA0, 1edmB0, 1ek6A0, 1tc1B0, 1ftrA0, 1b6g00, 1qcxA0, 2nacA0, 1arb00, 1mugA0, 1btkA0, 1yge00, 1uae00, 1uch00, 3sdhA0, 1opd00, 1gsoA0, 1di6A0, 1hxn00, 1byqA0, 1c02A0, 1ra900, 2arcB0, 1dpsD0, 1lbu00, 1dqsA0, 1kptA0, 1byi00, 1b16A0, 1pda00, 1d3vA0, 1qipB0, 1evhA0, 1atlA0, 1b0uA0]|0.40:0.50:[1xwl00, 1a2pA0, 1vsrA0, 1aop00, 3pte00, 1lst00, 1dxgA0, 2eng00, 1bs0A0, 1rcf00, 1qftA0, 1tfe00, 1qu9A0, 1kveA0, 1qhfB0, 1c24A0, 2cbp00, 1a8i00, 1vjs00, 4eugA0, 1aac00, 1cl8A0, 1bi5A0, 1qs1A0, 2cpp00, 1ytbA0, 1qk5A0, 2dpmA0, 1fmb00, 1edg00, 1uxy00, 3pviA0, 1qusA0, 1tud00, 1qauA0, 1ido00, 1h2rL0, 1a2zA0, 2act00]|0.50:0.60:[1chd00, 1qgiA0, 1ush00, 1cvl00, 1koe00, 1dcs00, 1moq00, 2gar00, 1bm800]|0.60:0.70:[2msbA0, 1ckaA0, 1tyv00, 1nddB0, 1d2nA0, 1qd1B0, 1avwB0, 1bg600, 1qhvA0, 1d7pM0, 1a28B0, 1atzA0, 1xjo00, 2bopA0, 1qtsA0, 1svy00, 1mjhB0, 1ayl00, 1fds00, 3htsB0]|0.70:0.80:[]|0.80:0.90:[]|0.90:1.00:[]|";
        Histogram h = Histogram.fromString(s);
        System.out.println(h);
    }
    
    @Test
    public void testShift() {
        List<String> values = vals("A", "B", "C", "D");
        Histogram from = new Histogram(10);
        from.add(values.get(0), 0.15);
        from.add(values.get(1), 0.25);
        from.add(values.get(2), 0.35);
        from.add(values.get(3), 0.45);
        
        Histogram to = new Histogram(10);
        to.add(values.get(0), 0.25);
        to.add(values.get(1), 0.35);
        to.add(values.get(2), 0.45);
        to.add(values.get(3), 0.55);
        
        int shift = from.compareShifts(to);
        System.out.println(shift);
    }
    
    private List<String> vals(String... vals) {
        List<String> values = new ArrayList<>();
        for (String val : vals) {
            values.add(val);
        }
        return values;
    }

}
