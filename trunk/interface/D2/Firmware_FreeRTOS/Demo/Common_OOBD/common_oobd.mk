SVNVERSION=$(warning Look for SVN revision)$(shell $(OOBD_COMMON_DIR)/getrev.sh)

# OOBD common  source files.
OOBD_COMMON_SOURCE = $(OOBD_COMMON_DIR)/main.c \
		$(OOBD_COMMON_DIR)/od_base.c \
		$(OOBD_COMMON_DIR)/odb_can.c \
		$(OOBD_COMMON_DIR)/od_outputTask.c \
		$(OOBD_COMMON_DIR)/odp_uds.c \
		$(OOBD_COMMON_DIR)/od_serial.c
