__all__ = [
'4000_ShadowWeapons',
'5011_l2day',
'6050_KetraOrcSupport',
'6051_VarkaSilenosSupport',
'7000_HeroItems',
'8000_RaidbossInfo'
]
print ""
print "importing custom data ..."
for name in __all__ :
    try :
        __import__(name,globals(), locals(), [], -1)
    except :
        print "failed to import quest : ",name
print "... done"
print ""